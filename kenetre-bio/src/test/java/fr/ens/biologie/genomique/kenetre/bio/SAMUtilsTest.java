/*
 *                  Eoulsan development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public License version 2.1 or
 * later and CeCILL-C. This should be distributed with the code.
 * If you do not have a copy, see:
 *
 *      http://www.gnu.org/licenses/lgpl-2.1.txt
 *      http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.txt
 *
 * Copyright for this code is held jointly by the Genomic platform
 * of the Institut de Biologie de l'École normale supérieure and
 * the individual authors. These should be listed in @author doc
 * comments.
 *
 * For more information on the Eoulsan project and its aims,
 * or to join the Eoulsan Google group, visit the home page
 * at:
 *
 *      http://outils.genomique.biologie.ens.fr/eoulsan
 *
 */

package fr.ens.biologie.genomique.kenetre.bio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMLineParser;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SAMUtilsTest {

  @Test
  public void readSAMHeaderTest() {

    final InputStream is = SAMUtilsTest.class.getResourceAsStream("/mapper_results_SE.sam");

    String s =
        "@HD\tVN:1.5\n"
            + "@SQ\tSN:chr1\tLN:197195432\n"
            + "@SQ\tSN:chr2\tLN:181748087\n"
            + "@SQ\tSN:chr3\tLN:159599783\n"
            + "@SQ\tSN:chr4\tLN:155630120\n"
            + "@SQ\tSN:chr5\tLN:152537259\n"
            + "@SQ\tSN:chr6\tLN:149517037\n"
            + "@SQ\tSN:chr7\tLN:152524553\n"
            + "@SQ\tSN:chr8\tLN:131738871\n"
            + "@SQ\tSN:chr9\tLN:124076172\n"
            + "@SQ\tSN:chr10\tLN:129993255\n"
            + "@SQ\tSN:chr11\tLN:121843856\n"
            + "@SQ\tSN:chr12\tLN:121257530\n"
            + "@SQ\tSN:chr13\tLN:120284312\n"
            + "@SQ\tSN:chr14\tLN:125194864\n"
            + "@SQ\tSN:chr15\tLN:103494974\n"
            + "@SQ\tSN:chr16\tLN:98319150\n"
            + "@SQ\tSN:chr17\tLN:95272651\n"
            + "@SQ\tSN:chr18\tLN:90772031\n"
            + "@SQ\tSN:chr19\tLN:61342430\n"
            + "@SQ\tSN:chrX\tLN:166650296\n"
            + "@SQ\tSN:chrY\tLN:15902555\n"
            + "@SQ\tSN:chrMT\tLN:16299\n";

    assertEquals(s, SAMUtils.readSAMHeader(is));
  }

  @Test
  public void createGenomeDescriptionFromSAM() {

    final InputStream is = SAMUtilsTest.class.getResourceAsStream("/mapper_results_SE.sam");

    final GenomeDescription desc = SAMUtils.createGenomeDescriptionFromSAM(is);

    assertEquals(22, desc.getSequenceCount());
    assertFalse(desc.containsSequence("chr102"));

    assertTrue(desc.containsSequence("chr1"));
    assertNotSame(197195431, desc.getSequenceLength("chr1"));
    assertEquals(197195432, desc.getSequenceLength("chr1"));
    assertNotSame(197195433, desc.getSequenceLength("chr1"));

    assertEquals(197195432, desc.getSequenceLength("chr1"));
    assertEquals(181748087, desc.getSequenceLength("chr2"));
    assertEquals(159599783, desc.getSequenceLength("chr3"));
    assertEquals(155630120, desc.getSequenceLength("chr4"));
    assertEquals(152537259, desc.getSequenceLength("chr5"));
    assertEquals(149517037, desc.getSequenceLength("chr6"));
    assertEquals(152524553, desc.getSequenceLength("chr7"));
    assertEquals(131738871, desc.getSequenceLength("chr8"));
    assertEquals(124076172, desc.getSequenceLength("chr9"));
    assertEquals(129993255, desc.getSequenceLength("chr10"));
    assertEquals(121843856, desc.getSequenceLength("chr11"));
    assertEquals(121257530, desc.getSequenceLength("chr12"));
    assertEquals(120284312, desc.getSequenceLength("chr13"));
    assertEquals(125194864, desc.getSequenceLength("chr14"));
    assertEquals(103494974, desc.getSequenceLength("chr15"));
    assertEquals(98319150, desc.getSequenceLength("chr16"));
    assertEquals(95272651, desc.getSequenceLength("chr17"));
    assertEquals(90772031, desc.getSequenceLength("chr18"));
    assertEquals(61342430, desc.getSequenceLength("chr19"));
    assertEquals(166650296, desc.getSequenceLength("chrX"));
    assertEquals(15902555, desc.getSequenceLength("chrY"));
    assertEquals(16299, desc.getSequenceLength("chrMT"));
  }

  @Test
  public void newSAMFileHeader() {

    final GenomeDescription desc = new GenomeDescription();

    desc.addSequence("chr1", 197195432);
    desc.addSequence("chr2", 181748087);
    desc.addSequence("chr3", 159599783);

    final SAMFileHeader header = SAMUtils.newSAMFileHeader(desc);

    assertEquals(3, header.getSequenceDictionary().size());

    assertNotSame(197195431, header.getSequence("chr1").getSequenceLength());
    assertEquals(197195432, header.getSequence("chr1").getSequenceLength());
    assertNotSame(197195433, header.getSequence("chr1").getSequenceLength());

    assertEquals(181748087, header.getSequence("chr2").getSequenceLength());
    assertEquals(159599783, header.getSequence("chr3").getSequenceLength());
  }

  @Test
  public void newSAMSequenceDictionaryTest() {

    final GenomeDescription desc = new GenomeDescription();

    desc.addSequence("chr1", 197195432);
    desc.addSequence("chr2", 181748087);
    desc.addSequence("chr3", 159599783);

    final SAMSequenceDictionary dict = SAMUtils.newSAMSequenceDictionary(desc);

    assertEquals(3, dict.size());

    assertNotSame(197195431, dict.getSequence("chr1").getSequenceLength());
    assertEquals(197195432, dict.getSequence("chr1").getSequenceLength());
    assertNotSame(197195433, dict.getSequence("chr1").getSequenceLength());

    assertEquals(181748087, dict.getSequence("chr2").getSequenceLength());
    assertEquals(159599783, dict.getSequence("chr3").getSequenceLength());
  }

  @Test
  public void createGenomeDescriptionFromSAMTest() {

    final SAMFileHeader header = new SAMFileHeader();
    GenomeDescription desc = SAMUtils.createGenomeDescriptionFromSAM(header);

    assertEquals(0, desc.getSequenceCount());

    final List<SAMSequenceRecord> sequences = new ArrayList<>();

    sequences.add(new SAMSequenceRecord("chr1", 197195432));
    sequences.add(new SAMSequenceRecord("chr2", 181748087));
    sequences.add(new SAMSequenceRecord("chr3", 159599783));

    header.setSequenceDictionary(new SAMSequenceDictionary(sequences));

    desc = SAMUtils.createGenomeDescriptionFromSAM(header);

    assertEquals(3, desc.getSequenceCount());
    assertEquals(197195432, desc.getSequenceLength("chr1"));
    assertEquals(181748087, desc.getSequenceLength("chr2"));
    assertEquals(159599783, desc.getSequenceLength("chr3"));
  }

  @Test
  public void parseIntervalTest() throws BadBioEntryException {

    SAMFileHeader header = new SAMFileHeader();
    header.addSequence(new SAMSequenceRecord("10", 133797422));
    SAMLineParser parser = new SAMLineParser(header);

    String[] samEntry = {
      "71f46d6e-bdbd-4a17-894f-e26367e08f50",
      "16",
      "10",
      "46875687",
      "24",
      "87S47=2D2=1I81=113N13=1I81=890N86=1777N28=1D62=1X56=1737N52=1I3=1X46=1X112=2305N20=1X4=2I24=1D49=4932N4=2D108=1D68=4D64=1X29=2D61=5955N14=3I54=3I46=1I51=1D62=1X230=1X16=1D37=1D1=1X27=1I7=1I56=131S",
      "*",
      "0",
      "0",
      "GCTACGTATTGCTGGTGCTGTTCGGATTCTATCGTTTTCCCTATTAACCTTTCTGTTGGTGCTGATATTGCTGCCATTACGGCCGGGGCTTGGGCTGGGGACATGCCCAGATCTCTCACATAGTTGCTATTGTTGCTTTATGAACACCTAATCTTATTGCTCAGTGGAGAATACAATCTAAACAAACAGGTGAATACAGAAGACAGTGCCAGTCGGAGGGTGGCATTAACACCCAGTGTGCCATATGGTGGTATCTATGTGAAATCCATTGTTCCTGGAGGACCAGCTGCCAAGGAAGGGCAGATCCTACAGGGTGACCGACTCCTGCAGGTGGATGGAGTGATTCTGTGCGGCCTCACCCACAAGCAGGCTGTGCAGTGCCTGAAGGGTCCTGGGCAGGTTGCAAGACTGGTCTTAGAGAGAAGAGCCCCAGGAGTACACAGCAGTGTCCTTCTGCTAATGACAGCATGGGAGATGAACGCACGGCTGCTTCCTTGGTAACAGCCTTGCCTGGCAGGCCTTCGAGCTGTGTCTCGGTGACAGATGGTCCTAAGTTTGAAGTCAAACTAAAAAAGAATGCCAATGGTTTGGGATTCAGTTTTTGTGCAGATGGAGAAAGAGAGCTGCAGCCATCTCAAAAGTGATCTTGCGAGGATTAAGAGGCTCTTTCCGGGGCAGCCAGCTGAGGAGAATGGGGCCATTGCAGCTGGTGACATTATCCTGGCCGTGAATGGAAGGTCCACGGAAGGCCTCATCTTCCAGGAGGTGCTGCATTTACTGAGGGGGGCCCCCCACAGGAAGTCACGCTCCTCCTTGCCGACCCCCTCCAGGTGCGCTGCCTGAGATGGAGCAGGAATGGCAGACACGAACTCTCAGCTGACAAAGAATTCACCAGGGCAACATGTACTGACTCATGTACCAGCCCCATCCTGGATCAAGAGGACAGCTGGAGGGACAGTGCCTCCCCAGATGCAGGGAAGGCCTGGGTCTCAGGCCAGAGTCTTCCCAAAAGGCCATCAGAGAGGCACAATGGGGCCAAAACAGAGACCTTGGGCCAGTTCCTTGACACATTCTCCTGAGTCCCACCCTCATTTATGCAAACTTCATCAAGAAAGGGATGAATCAACATTGGCGACCTTTGGAAAAGGATGTGAGGCAAAACTGCTATTCAGTTTGTGATATCATGAGACTTGGAAGATATTCCTTCTCATCTCCTCCTCTAACCAGACTTTCGACAGATATTTTCTGAGCACCTTCTCTGCATGTCTGCAGCAGTGCTGTGTAAAATGCCCTACCTTTGCATGGACTATTCTTTCTCAATCAAGAGGCGTGTGTGGCGAACTTGGGGCAGCCCCTGGAAGTCTTGTTCTTGACCATTACGTCTGCGGCTGCATCACCAGATAATGAGCTTCACCACTTGTCTGCCTCCTGCGTCCTTCCGCGGGGAGTAAATGTCACTTCAGCTTGCCGCATCTCTAAATAGGCAAATTTTCAGTGCTCAGAAAAGGACCTGATCTTTGCACAAAGTGCTTTGATGGTTGCCTGCTTGAGTCACTCCCAATCCCTTCCTGAAGCCCTTTCTTTATAATTCTTCTGTTGAAATAGCCATCATATTCACAGTACTAATCACAGCATCTCACATTTACTAAAAACTTACCCCATTCCAGGAACCCAGAGTTGGGGGGCTGTGTCAGAATTATGTAATTTACGTGTCCCACAATCCTAGATGCTTCTTGACCATCTAGTTTTTGTCAAAATGAGAAAACTGAGGTTCCAAAGAAGTCAATAAACTTGTCCAAAGTCTGACCGAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGAAGATAGGAGTTGACAGGCAAGTAGGTTAATAGGGAAACACGATAGAATCCGAACAGCACCAGTACGTAACTGAACGAAGTACAA",
      "%&(((13468:99453334445442***01789;C@501=>A@?:471*,3;<<;;;99:;;?>>=;<<;<>><<97754322358778778;;<5689:=<;;;;=A?>>;8==>><<433000//003++++.++:;;<<;::::<?==;<<>====888<<:::<<==:::9883/:;<...../3;===>>>>>===>==;:;;;944446:77=<<=?>>>?=;-))9<=?<<<<;>><<=;;<<=>3'''(-6:>>?><<<;;;<<<<=;:<;;<=<<<=>?=@<;;<<;;;;;<::;:9;::::;>=65555::76788=?==>==<43431,*+++.1:76668>>?>>>>>><;==<>>>:::;:885446223>>;:975488=>?**))))(&&%*+44<:32233;;621-,+*)*--59:;:;==@A?@@@??<<;::<<<?>>>>@BA@>>>=;;;:;<<=8666556101+*++-;<==A>=<<<=<96666:96444::;==:9888=998999>=<:<;656666=>=<<::;A@?@???@?>@>4;98611;>=?>=98989;;;=><>====<=;;;8-,++,,32,&''()4<734999955557?;;;::=:=:;8>>;9:877545557<:((''((269::51243259:;<<<==<<<:9:99:99899;;;<=<=>>>>>??@????==>======977779>?<=<<<;;;<99999<<<<<>>>@?@@???>A><;;.---.5400188788887:8866/643347889<<<4/*****,0+,))15667:315<=99:86788=:AA@@:;<??>==>@@@A@>=<;;<<>;5)%%%'((((48>=>>=<<6===>?>?>==><<=;:::;<;;;;;<9984*)(()--,..123@;>=<;;=??>>====>?=<:7666667?>>>:66533<77765--55*';<;;<<;<:66<=:;;<<<<<>>??@>==8==97>;;;<==<=<<:88979:9::88778;?@<>:520+4::<=>>??=<<;:?@33B@==>B;=70-.89>>===<<30136<=>><<<=<4?==7,+++,/00-=<;<<=>>=?>=>>??<6333+)(*'*))-;5<44;4///=<;;<;?@?65222652''''(7:<=?>=<==>?@@?==???@?>=>=<;<=<>=?>>74)(-+,9;==>>===<<;955558<<====21<112;??==;6300088;++****+*))*...///*(*76;@@;::9998796/2:999::::==<<>8..'''22>>>>><557777/==:77779<;::;:::::9766688=>>>?@<;87.))17==>433,,,,-5555<A?@@@>>====??=>@><<<<<==;6555012<;::57/.66599:9965545657=;:9;;@>=<:1333469:98544446<=<:;;<@=<<=<===<<:99==<<==><<<@=966,+++''))0'<=<=63145,,,++17691111298799::<=====>=====<<999849:=?<=<;:?=8>E?7ADA@<>=B=<>==?>@B@B@=<;;;<<==??@@?=>;:;<<>@@?>>???>=>==A@?===>>>?AD>>=:99999<==>>=<:9962,++,41655529;999:;?=?BACB@>5440/-,,,,---08;:(((((;<=<>?>===<=<:999899:;6-+,.<<>>=<8.)5211;93.544448989==<===;;;;<??A@>=>==<<;;<><;;;;;6.,,,12467775211100010//////.+*)()+469:;<=???>;79244599:,+++22213=:::::<<:9:::<?A;94347.7654448==65321128===0/0/('&&)));<==<855558?65)('''",
      "s1:i:1551",
      "s2:i:1496",
      "NM:i:38",
      "AS:i:1501",
      "de:f:0.0156",
      "rl:i:69",
      "cm:i:435",
      "nn:i:0",
      "tp:A:P",
      "ms:i:1622",
      "ts:A:-"
    };

    String samEntryString1 = String.join("\t", samEntry);

    samEntry[5] =
        "87S47M2D2M1I81M113N13M1I81M890N86M1777N28M1D119M1737N52M1I163M2305N25M2I24M1D49M4932N4M2D108M1D68M4D94M2D61M5955N14M3I54M3I46M1I51M1D310M1D37M1D29M1I7M1I56M131S";
    String samEntryString2 = String.join("\t", samEntry);

    SAMRecord aln1 = parser.parseLine(samEntryString1);
    SAMRecord aln2 = parser.parseLine(samEntryString2);
    BEDEntry bed1 = SAMUtils.parseIntervalsToBEDEntry(aln1);
    BEDEntry bed2 = SAMUtils.parseIntervalsToBEDEntry(aln2);

    BEDEntry expected1 = new BEDEntry();
    expected1.parse(
        "10\t46875686\t46895120\t71f46d6e-bdbd-4a17-894f-e26367e08f50\t\t-\t46875686\t46895120\t0\t8\t132,94,86,148,215,99,344,607\t0,245,1229,3092,4977,7497,12528,18827");

    assertEquals(expected1, bed1);
    assertEquals(expected1, bed2);
  }
}
