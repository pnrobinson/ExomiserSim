package org.jax;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.google.protobuf.util.JsonFormat;
import org.jax.exomisersim.ExomiserRunner;
import org.jax.exomisersim.VcfSimulator;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.Disease;
import org.phenopackets.schema.v1.core.HtsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Run one or multiple phenopacket simulations
 */
@Parameters(commandDescription = "Simulate VCF Exomiser analysis from phenopacket")
public class ExomiserSim {
    private static final Logger logger = LoggerFactory.getLogger(ExomiserSim.class);
    //TODO -- Get this from the VCF file or from the Phenopacket or from command line
    String genomeAssembly = "GRCh37";
    @Parameter(names = {"-p", "--phenopacket"}, description = "path to phenopacket file")
    private String phenopacketPath;
    @Parameter(names = {"-e", "--exomiser"}, description = "path to the Exomiser data directory", required = true)
    private String exomiserPath;
    @Parameter(names = {"-v", "--template-vcf"}, description = "path to template VCF file", required = true)
    private String templateVcfPath;
    @Parameter(names = {"--phenopacket-dir"}, description = "path to directory with multiple phenopackets")
    private String phenopacketDir;
    @Parameter(names = {}, description = "name of the output file with simulation results")
    private String simulationOutFile = "exomiser_simulation_results.txt";
    private boolean hasVcf;
    private String simulatedDisease = null;

    public ExomiserSim() {

    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        ExomiserSim esim = new ExomiserSim();
        JCommander jc = JCommander.newBuilder()
                .addObject(esim)
                .build();
        jc.setProgramName("java -jar ExomiserSim.jar");
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            // Note that by default, JCommand is OK with -h download but
            // not with download -h
            // The following hack makes things work with either option.
            String mycommand = null;
            String commandstring = String.join(" ", args);

            if (commandstring == null) { // user ran without any command
                jc.usage();
                System.exit(0);
            }
            System.err.println("[ERROR] " + e.getMessage());
            System.err.println("[ERROR] your command: " + commandstring);
            System.err.println("[ERROR] enter java -jar Lr2pg -h for more information.");
            System.exit(1);
        }
        esim.startSimulation();

    }




    private static Phenopacket readPhenopacket(String phenopacketPath) {
        Path ppPath = Paths.get(phenopacketPath);
        Phenopacket.Builder ppBuilder = Phenopacket.newBuilder();
        try (BufferedReader reader = Files.newBufferedReader(ppPath)) {
            JsonFormat.parser().merge(reader, ppBuilder);
        } catch (IOException e) {
            logger.warn("Unable to read/decode file '{}'", ppPath);
            throw new RuntimeException(String.format("Unable to read/decode file '%s'", ppPath));
        }
        return ppBuilder.build();
    }

    public void startSimulation() {
        if (this.phenopacketPath != null) {
            logger.info("Running single file Phenopacket/VCF simulation at {}", phenopacketPath);
            runOneVcfAnalysis(this.phenopacketPath);
        } else if (this.phenopacketDir != null) {
            logger.info("Running Phenopacket/VCF simulations at {}", phenopacketDir);
            final File folder = new File(phenopacketDir);
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isFile() && fileEntry.getAbsolutePath().endsWith(".json")) {
                    logger.info("\tPhenopacket: \"{}\"", fileEntry.getAbsolutePath());
                    runOneVcfAnalysis(fileEntry.getAbsolutePath());
                }
            }
        } else {
            System.err.println("[ERROR] Either the --phenopacket or the --phenopacket-dir option is required");
            throw new RuntimeException("[ERROR] Either the --phenopacket or the --phenopacket-dir option is required");
        }

    }




    private void runOneVcfAnalysis(String phenopacketAbsolutePath) {

        Phenopacket pp = readPhenopacket(phenopacketAbsolutePath);
        VcfSimulator vcfSimulator = new VcfSimulator(Paths.get(this.templateVcfPath));
        try {
            HtsFile htsFile = vcfSimulator.simulateVcf(pp.getSubject().getId(), pp.getVariantsList(), genomeAssembly);
            pp = pp.toBuilder().clearHtsFiles().addHtsFiles(htsFile).build();
        } catch (IOException e) {
            throw new RuntimeException("Could not simulate VCF for phenopacket");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();

        hasVcf = pp.getHtsFilesList().stream().anyMatch(hf -> hf.getHtsFormat().equals(HtsFile.HtsFormat.VCF));
        if (pp.getDiseasesCount() != 1) {
            System.err.println("[ERROR] to run this simulation a phenoopacket must have exactly one disease diagnosis");
            System.err.println("[ERROR]  " + pp.getSubject().getId() + " had " + pp.getDiseasesCount());
            return; // skip to next Phenopacket
        }
        if (!hasVcf) {
            System.err.println("[ERROR] Could not simulate VCF"); // should never happen
            return; // skip to next Phenopacket
        }
        File vcfFile = vcfSimulator.getSimulatedVcfOutPath();
        ExomiserRunner runner = new ExomiserRunner(this.exomiserPath,vcfFile,pp);
        runner.writeYAML();
        runner.runExomiser();

    }


    /*
     private int extractRank(String path) {
        int rank = -1;
        int n_over_50=0; // number of differentials with post prob over 50%
        int n_total=0; // total number of differentials
        LiricalRanking lr=null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("!")) continue;
                if (line.startsWith("rank")) continue;
                String[] fields = line.split("\t");
                rank = Integer.parseInt(fields[0]);
                String diseaseName = fields[1];
                String diseaseCurie = fields[2];
                String posttest = fields[4].replace("%","");// remove the percent sign
                if (diseaseCurie.equals(this.simulatedDisease)) {
                    logger.info("Got rank of {} for simulated disease {}", rank, simulatedDisease);
                    lr = new LiricalRanking(rank, line);
                    rankingsList.add(lr);
                }
                Double posttestprob = Double.parseDouble(posttest);
                if (posttestprob>50.0) n_over_50++;
                n_total++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = String.format("%s: posttest prob > 50%%: %d/%d",this.simulatedDisease,n_over_50,n_total);
        if (lr!=null) {
            lr.addExplanation(res);
        }
        logger.info(res);
        // We should never get here. If we do, then probably the OMIM id used in the Phenopacket
        // is incorrect or outdated.
        // This command is not intended for general consumption. Therefore, it is better
        // to terminate the program and correct the error rather than just continuing.
        if (rank==-1) {
            System.err.println("[ERROR] Could not find rank of simulated disease \"" +simulatedDisease + "\"");
            System.exit(1);
        }
        return rank;
    }

     */



}
