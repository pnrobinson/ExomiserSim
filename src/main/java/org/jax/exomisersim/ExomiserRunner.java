package org.jax.exomisersim;

import org.phenopackets.schema.v1.Phenopacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ExomiserRunner {
    private static final Logger logger = LoggerFactory.getLogger(ExomiserRunner.class);

    private final String pathToExomiser;
    private final File simulatedVcfFile;
    private final Phenopacket phenopacket;

    private final int threadNum=1;

    private String stdin = null;

    private String stderr = null;

    public ExomiserRunner(String exomiser, String exomiserData, File vcfFile , Phenopacket ppacket){
        this.pathToExomiser = exomiser;
        this.simulatedVcfFile=vcfFile;
        this.phenopacket=ppacket;


    }



    public void writeYAML() {
        final File yamlPath = new File("simulation.yaml");
        logger.info("Writing YAML file to {}", yamlPath);
        //yamlPath.deleteOnExit();
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
            writer.close();
            logger.info("Closing "+yamlPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open YAML file for writing");
        }
    }



    public void runExomiser() {
        String[] args = new String[6];
        args[0]="java";
        args[1]="-jar";
        args[2]=pathToExomiser;
        args[3]="--exomiser.data-directory=/home/peter/data/exomiser";
        args[4]="--analyze";
        args[5]="simulation.yaml"; // TODO make this parameter



        String btcomd= Arrays.stream(args).collect(Collectors.joining(" "));
        logger.trace("Running: "+btcomd);

        String[] dummy=new String[0];
        try {
            // we need to provide the Runtime with the directory in which to start
            // Since we are providing the absolute path of bowtie, we will start in the
            // current working directory (".").
            Process process = Runtime.getRuntime().exec(args,dummy,new File("."));
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            String s=null;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s+"\n");
            }
            this.stdin=sb.toString();
            sb = new StringBuilder();
            while ((s = stdError.readLine()) != null) {
                sb.append(s+"\n");
            }
            if (sb.length()>0)
                this.stderr=sb.toString();
        } catch (IOException e) {
            String msg = String.format("Could not run bowtie [%s]",e.getMessage());
            throw new RuntimeException(msg);
        }
        System.out.println("STDOUT="+stdin);
        System.out.println("STDERR="+stderr);

    }



}
