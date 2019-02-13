package org.jax.grid;

import com.google.common.collect.ImmutableList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jax.Phenomiser;
import org.jax.services.AbstractResources;
import org.jax.utils.DiseaseDB;
import org.monarchinitiative.phenol.formats.hpo.HpoAnnotation;
import org.monarchinitiative.phenol.formats.hpo.HpoDisease;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.Item2PValue;

import java.util.*;

import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getDescendents;
import static org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm.getParentTerms;

/**
 * A simulator that simulates cases from the {@link HpoDisease} objects by choosing a subset of terms
 * and adding noise terms.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class PhenotypeOnlyHpoCaseSimulator {
    private static final Logger logger = LogManager.getLogger(PhenotypeOnlyHpoCaseSimulator.class);
    /** An object representing the Human Phenotype Ontology */
    private Ontology ontology;
    /** A list of all HPO term ids in the Phenotypic abnormality subontology. */
    private final ImmutableList<TermId> phenotypeterms;
    /** Key: diseaseID, e.g., OMIM:600321; value: Corresponding HPO disease object. */
    private final Map<TermId,HpoDisease> diseaseMap;
    /** Number of HPO terms to use for each simulated case. */
    private final int n_terms_per_case;
    /** Number of "noise" (unrelated) HPO terms to use for each simulated case. */
    private final int n_noise_terms;
    /** Number of cases to simulate. */
    private final int n_cases_to_simulate;
    /** If true, we exchange each of the non-noise terms with a direct parent except if that would mean going to
     * the root of the phenotype ontology.*/
    private boolean addTermImprecision = false;
    /** The proportion of cases at rank 1 in the current simulation */
    private double proportionAtRank1=0.0;
    /** Case currently being simulated/analyzed. */
    //private HpoCase currentCase;
    /** This array will hold the TermIds from the disease map in order -- this will allow us to
     * get random indices for the simulations. */
    private TermId[] termIndices;
    /** If true, show lots of results in STDOUT while we are calculating. */
    private boolean verbose=true;
    /** Root term id in the phenotypic abnormality subontology. */
    private final static TermId PHENOTYPIC_ABNORMALITY = TermId.of("HP:0000118");

    public PhenotypeOnlyHpoCaseSimulator(AbstractResources resources,
                                         int cases_to_simulate,
                                         int terms_per_case,
                                         int noise_terms,
                                         boolean imprecise) {
        this.n_cases_to_simulate=cases_to_simulate;
        this.n_terms_per_case=terms_per_case;
        this.n_noise_terms=noise_terms;
        this.ontology=resources.getHpo();
        this.diseaseMap=resources.getDiseaseMap();
        Set<TermId> descendents=getDescendents(ontology,PHENOTYPIC_ABNORMALITY);
        ImmutableList.Builder<TermId> builder = new ImmutableList.Builder<>();
        for (TermId t: descendents) {
            builder.add(t);
        }
        this.phenotypeterms=builder.build();
        this.termIndices=diseaseMap.keySet().toArray(new TermId[0]);
        this.addTermImprecision = imprecise;
    }

    public void setVerbosity(boolean v) { this.verbose=v;}

    /** @return the proportion of all simulated cases at rank 1.*/
    public double getProportionAtRank1() {
        return proportionAtRank1;
    }

    public TermId getNextRandomDisease(Random r) {
        int i = r.nextInt(diseaseMap.size());
        TermId tid = termIndices[i];
        HpoDisease disease = diseaseMap.get(tid);
        while (disease.getPhenotypicAbnormalities().size() < this.n_terms_per_case) {
            i = r.nextInt(diseaseMap.size());
            tid = termIndices[i];
            disease = diseaseMap.get(tid);
        }
        return tid;
    }



    /** This will run simulations according to the parameters {@link #n_cases_to_simulate},
     * {@link #n_terms_per_case} and {@link #n_noise_terms}.n
     */
    public void simulateCases() {
        int c=0;
        Map<Integer,Integer> ranks=new HashMap<>();
        logger.trace(String.format("Simulating n=%d HPO cases with %d random terms and %d noise terms per case.",n_cases_to_simulate,n_terms_per_case,n_noise_terms));
        int size = diseaseMap.size();

        Random r = new Random();

        for (int i=0;i<n_cases_to_simulate;++i) {
            TermId diseaseToSimulate = getNextRandomDisease(r);//termIndices[randomIndices[i]];
            HpoDisease disease = diseaseMap.get(diseaseToSimulate);

            if (disease.getNumberOfPhenotypeAnnotations() <this.n_terms_per_case) {
                logger.trace(String.format("Skipping disease %s [%s] because it has no phenotypic annotations",
                        disease.getName(),
                        disease.getDiseaseDatabaseId()));
                continue;
            }
            System.out.println(disease);
            int rank = simulateCase(disease);
            if (verbose) {
                System.err.println(String.format("%s: rank=%d", disease.getName(), rank));
            }
            ranks.putIfAbsent(rank,0);
            ranks.put(rank, ranks.get(rank) + 1);
            if (++c>n_cases_to_simulate) {
                break; // finished!
            }
        }
        /*if (ranks.containsKey(1)) {
            proportionAtRank1 = ranks.get(1) / (double) n_cases_to_simulate;
        } else {
            proportionAtRank1 = 0.0;
        }*/

        if (ranks.containsKey(0)) {
            proportionAtRank1 = ranks.get(0) / (double) n_cases_to_simulate;
        } else {
            proportionAtRank1 = 0.0;
        }
        if (verbose) {
            dump2shell(ranks);
        }


    }


    private void dump2shell(Map<Integer,Integer> ranks) {
        int N=n_cases_to_simulate;
        int rank11_20=0;
        int rank21_30=0;
        int rank31_100=0;
        int rank101_up=0;
        System.out.println();
        System.out.println();
        System.out.println(String.format("Simulation of %d cases with %d HPO terms, %d noise terms. Imprecision: %s",
                n_cases_to_simulate,n_terms_per_case,n_noise_terms,addTermImprecision));
        for (int r:ranks.keySet()) {
           /* if (r==1) {
                proportionAtRank1=ranks.get(r) / (double)N;
            }*/
            if (r==0) {
                proportionAtRank1=ranks.get(r) / (double)N;
            }
            if (r<11) {
                System.out.println(String.format("Rank=%d: count:%d (%.1f%%)", r, ranks.get(r), 100.0 * ranks.get(r) / N));
            } else if (r<21) {
                rank11_20+=ranks.get(r);
            } else if (r<31) {
                rank21_30+=ranks.get(r);
            } else if (r<101) {
                rank31_100+=ranks.get(r);
            } else {
                rank101_up+=ranks.get(r);
            }
        }
        System.out.println(String.format("Rank=11-20: count:%d (%.1f%%)", rank11_20, (double) 100* rank11_20 / N));
        System.out.println(String.format("Rank=21-30: count:%d (%.1f%%)", rank21_30, (double) 100 * rank21_30 / N));
        System.out.println(String.format("Rank=31-100: count:%d (%.1f%%)", rank31_100, (double) 100 * rank31_100 / N));
        System.out.println(String.format("Rank=101-...: count:%d (%.1f%%)", rank101_up, (double) 100 * rank101_up / N));
    }




    /**
     * This is a term that was observed in the simulated patient (note that it should not be a HpoTermId, which
     * contains metadata about the term in a disease entity, such as overall frequency. Instead, we are simulating an
     * individual patient and this is a definite observation.
     * @return a random term from the phenotype subontology.
     */
    private TermId getRandomPhenotypeTerm() {
        int n=phenotypeterms.size();
        int r = (int)Math.floor(n*Math.random());
        return phenotypeterms.get(r);
    }

    /** @return a random parent of term tid. */
    private TermId getRandomParentTerm(TermId tid) {
        Set<TermId> parents = getParentTerms(ontology,tid,false);
        int r = (int)Math.floor(parents.size()*Math.random());
        int i=0;
        return (TermId)parents.toArray()[r];
    }



    public Ontology getOntology() {
        return ontology;
    }

    /**
     * This creates a simulated, phenotype-only case based on our annotations for the disease
     * @param disease Disease for which we will simulate the case
     * @return HpoCase object with a randomized selection of phenotypes from the disease
     */
    private List<TermId> getRandomTermsFromDisease(HpoDisease disease) {
        int n_terms=Math.min(disease.getNumberOfPhenotypeAnnotations(),n_terms_per_case);
        //int n_random=Math.min(n_terms, n_noise_terms);// do not take more random than real terms.
        logger.trace("Creating simulated case with n_terms="+n_terms + ", n_random="+n_noise_terms);
        // the creation of a new ArrayList is needed because disease returns an immutable list.
        List<HpoAnnotation> abnormalities = new ArrayList<>(disease.getPhenotypicAbnormalities());
        ImmutableList.Builder<TermId> termIdBuilder = new ImmutableList.Builder<>();
        Collections.shuffle(abnormalities); // randomize order of phenotypes
        // take the first n_random terms of the randomized list
        abnormalities.stream().limit(n_terms).forEach(a-> termIdBuilder.add(a.getTermId()));
        // now add n_random "noise" terms to the list of abnormalities of our case.
        for(int i=0;i<n_noise_terms;i++){
            TermId t = getRandomPhenotypeTerm();
            if (addTermImprecision) {
                t = getRandomParentTerm(t);
            }
            termIdBuilder.add(t);
        }
        return termIdBuilder.build();

    }


    public int simulateCase(HpoDisease disease) {
        int rank = -1;
        List<TermId> randomizedTerms = getRandomTermsFromDisease(disease);
        if (randomizedTerms.size() > 0 ) {
            randomizedTerms.forEach(System.out::println);
            List<Item2PValue<TermId>> result = Phenomiser.query(randomizedTerms, Arrays.asList(DiseaseDB.OMIM));
            //result.stream().forEach(r -> System.out.println(r.getItem().getValue()));

            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).getItem().equals(disease.getDiseaseDatabaseId())) {
                    rank = i;
                    break;
                }
            }
        }
        return rank;
    }


}
