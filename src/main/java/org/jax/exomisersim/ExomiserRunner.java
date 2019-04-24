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
    private final File exomiserDir;
    private final String applicationProp;
    private final File simulatedVcfFile;
    private final Phenopacket phenopacket;

    private final int threadNum=1;

    private String stdin = null;

    private String stderr = null;

    public ExomiserRunner(String exomiser, File vcfFile , Phenopacket ppacket){
        this.pathToExomiser = exomiser;
        this.simulatedVcfFile=vcfFile;
        this.phenopacket=ppacket;
        File f = new File(pathToExomiser);
        this.exomiserDir = f.getParentFile();
        this.applicationProp = exomiserDir.getAbsolutePath() + File.separator + "application.properties";

    }



    public void writeYAML() {
        final File yamlPath = new File("simulation.yml");
        logger.info("Writing YAML file to {}", yamlPath);
        //yamlPath.deleteOnExit();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(yamlPath));
            writer.write("analysis:\n");
            writer.write("  genomeAssembly: hg19\n"); // TODO -- don't hard code this!
            writer.write("  vcf: "+ this.simulatedVcfFile.getAbsolutePath() +"\n");
            writer.write("  ped:\n");
            writer.write("  proband:\n");
            writer.write("  analysisMode: PASS_ONLY\n");
            writer.write("  hpoIds: ['HP:0001156', 'HP:0001363', 'HP:0011304', 'HP:0010055']\n"); // TODO get from Phenopacket
            writer.write("  frequencySources: [\n" +
                    "    THOUSAND_GENOMES,\n" +
                    "    TOPMED,\n" +
                    "    UK10K,\n" +
                    "    ESP_AFRICAN_AMERICAN, ESP_EUROPEAN_AMERICAN, ESP_ALL,\n" +
                    "    EXAC_AFRICAN_INC_AFRICAN_AMERICAN, EXAC_AMERICAN,\n" +
                    "    EXAC_SOUTH_ASIAN, EXAC_EAST_ASIAN,\n" +
                    "    EXAC_FINNISH, EXAC_NON_FINNISH_EUROPEAN,\n" +
                    "    EXAC_OTHER,\n" +
                    "    GNOMAD_E_AFR,\n" +
                    "    GNOMAD_E_AMR,\n" +
                    "    GNOMAD_E_EAS,\n" +
                    "    GNOMAD_E_FIN,\n" +
                    "    GNOMAD_E_NFE,\n" +
                    "    GNOMAD_E_OTH,\n" +
                    "    GNOMAD_E_SAS,\n" +
//                    "    NOMAD_G_AFR,\n" +
//                    "    GNOMAD_G_AMR,\n" +
//                    "    GNOMAD_G_EAS,\n" +
//                    "    GNOMAD_G_FIN,\n" +
//                    "    GNOMAD_G_NFE,\n" +
//                    "    GNOMAD_G_OTH,\n" +
//                    "    GNOMAD_G_SAS\n" +
                    "  ]\n");
            writer.write("  pathogenicitySources: [POLYPHEN, MUTATION_TASTER, SIFT]\n");
            writer.write("  steps: [ \n" +
                    "    variantEffectFilter: {remove: [UPSTREAM_GENE_VARIANT, INTERGENIC_VARIANT, REGULATORY_REGION_VARIANT, CODING_TRANSCRIPT_INTRON_VARIANT," +
                    " NON_CODING_TRANSCRIPT_INTRON_VARIANT, SYNONYMOUS_VARIANT, DOWNSTREAM_GENE_VARIANT, SPLICE_REGION_VARIANT]},\n" +
                    "    frequencyFilter: {maxFrequency: 1.0},\n" +
                    "    pathogenicityFilter: {keepNonPathogenic: true},\n" +
                    "    inheritanceFilter: {},\n" +
                    "    omimPrioritiser: {},\n" +
                    "    #hiPhivePrioritiser: {},\n" +
                    "    # or run hiPhive in benchmarking mode: \n" +
                    "    #hiPhivePrioritiser: {runParams: 'mouse'},\n" +
                    "    #phivePrioritiser: {}\n" +
                    "    phenixPrioritiser: {}\n" +
                    "    #exomeWalkerPrioritiser: {seedGeneIds: [11111, 22222, 33333]}\n" +
                    "  ]\n");
            writer.write("  outputOptions:\n" +
                    "    outputPassVariantsOnly: false\n" +
                    "    numGenes: 0\n" +
                    "    outputPrefix: simulation\n" +  // TODO tailor prefix
                    "    outputFormats: [TSV-GENE, TSV-VARIANT, VCF, HTML]");
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
        args[3]=String.format("--spring.config.location=%s",applicationProp);
        args[4]="--analyze";
        args[5]="simulation.yml";



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
