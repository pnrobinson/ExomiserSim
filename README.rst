###########
ExomiserSim
###########


Prototype app to run the Exomiser with Phenopackets.
To run this app, export Phenopackets using the menu of HpoCaseAnnotator (currently 110)
or provide your own Phenopacket. We use a template VCF file (e.g. project.NIST.hc.snps.indels.NIST7035.vcf)
to which we will add a mutation.

Download Exomiser and exomiser data.

Run as follows ::

    $ java -jar ExomiserSim.java -p sample.phenopacket \
        -v template.vcf \
        --exomiser /path/to/exomiser-11.0.0.jar

Note that  and ``application.properties`` lives in the same directory as ``exomiser-11.0.0.jar``.
The application.properties must be adjusted to point
to the Exomiser data.