package org.jax;

import org.phenopackets.schema.v1.Phenopacket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExomiserRunner {

    private final String pathToExomiser;
    private final File simulatedVcfFile;
    private final Phenopacket phenopacket;

    public ExomiserRunner(String exomiser, String exomiserData, File vcfFile , Phenopacket ppacket){
        this.pathToExomiser = exomiser;
        this.simulatedVcfFile=vcfFile;
        this.phenopacket=ppacket;


    }



    public void writeYAML() {
        final File yamlPath = new File("simulation.yaml");
        yamlPath.deleteOnExit();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(yamlPath));
            writer.write("analysis:\n");
            writer.write("\tgenomeAssembly: hg19\n"); // TODO -- don't hard code this!
            writer.write("\tped:\n");
            writer.write("\tproband:\n");
            writer.write("\tanalysisMode: PASS_ONLY\n");
            writer.write("\thpoIds: ['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']"); // TODO get from Phenopacket
            writer.write("\t frequencySources: [\n" +
                    "\t\tTHOUSAND_GENOMES,\n" +
                    "\t\tTOPMED,\n" +
                    "\t\tUK10K,\n" +
                    "\t\tESP_AFRICAN_AMERICAN, ESP_EUROPEAN_AMERICAN, ESP_ALL,\n" +
                    "\t\tEXAC_AFRICAN_INC_AFRICAN_AMERICAN, EXAC_AMERICAN,\n" +
                    "\t\tEXAC_SOUTH_ASIAN, EXAC_EAST_ASIAN,\n" +
                    "\t\tEXAC_FINNISH, EXAC_NON_FINNISH_EUROPEAN,\n" +
                    "\t\tEXAC_OTHER,\n" +
                    "\t\tGNOMAD_E_AFR,\n" +
                    "\t\tGNOMAD_E_AMR,\n" +
                    "\t\tGNOMAD_E_EAS,\n" +
                    "        GNOMAD_E_FIN,\n" +
                    "        GNOMAD_E_NFE,\n" +
                    "        GNOMAD_E_OTH,\n" +
                    "\t\tGNOMAD_E_SAS,\n" +
                    "\t\tNOMAD_G_AFR,\n" +
                    "\t\tGNOMAD_G_AMR,\n" +
                    "\t\tGNOMAD_G_EAS,\n" +
                    "\t\tGNOMAD_G_FIN,\n" +
                    "\t\tGNOMAD_G_NFE,\n" +
                    "\t\tGNOMAD_G_OTH,\n" +
                    "\t\tGNOMAD_G_SAS\n" +
                    "\t]");
            writer.write("\tpathogenicitySources: [POLYPHEN, MUTATION_TASTER, SIFT]\n");
            writer.write("\tsteps: [ \n" +
                    "\t\tvariantEffectFilter: {remove: [UPSTREAM_GENE_VARIANT, INTERGENIC_VARIANT, REGULATORY_REGION_VARIANT, CODING_TRANSCRIPT_INTRON_VARIANT," +
                    " NON_CODING_TRANSCRIPT_INTRON_VARIANT, SYNONYMOUS_VARIANT, DOWNSTREAM_GENE_VARIANT, SPLICE_REGION_VARIANT]},\n" +
                    "\t\tfrequencyFilter: {maxFrequency: 1.0},\n" +
                    "\t\tpathogenicityFilter: {keepNonPathogenic: true},\n" +
                    "\t\tinheritanceFilter: {},\n" +
                    "\t\tomimPrioritiser: {},\n" +
                    "\t\thiPhivePrioritiser: {},\n" +
                    "\t\t# or run hiPhive in benchmarking mode: \n" +
                    "\t\t#hiPhivePrioritiser: {runParams: 'mouse'},\n" +
                    "\t\t#phivePrioritiser: {}\n" +
                    "\t\t#phenixPrioritiser: {}\n" +
                    "\t\t#exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}\n" +
                    "\t]\n");
            writer.write("\toutputOptions:\n" +
                    "\t\toutputPassVariantsOnly: false\n" +
                    "\t\tnumGenes: 0\n" +
                    "\t\toutputPrefix: results/simulation\n" +  // TODO tailor prefix
                    "\t\toutputFormats: [TSV-GENE, TSV-VARIANT, VCF, HTML]");

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open YAML file for writing");
        }





    }
}
