package pro.belbix.tim;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import pro.belbix.tim.protobuf.neuron.Nsrsi;

public class NSrsiProtoReader {

    public static void main(String[] args) {
        String s = "0AF8010801127B121C080110001801215C15569288C81DC0297C4D2291B3A019C03001380A121C08021000180121BDF72BA682CD2040294A224921290F2740300138071979503D09F69958402176E53ADFEAE816C0299348F18D2D43484031F81BC8E8A6AB39C03991B8F61D91E53B40499087AE92C4F25740511A8D2D6E4D1B10C01A5D08011259121808011801210FBA02C3290BF83F29293BA67CCD0B1040380819425AFD79A9FB5840219BE97F92BBCD6BBF29F511E0AE20FA484031C53170EFB099204039A151B1CED834244049945D929DBED85840514906D65F587FE13F22180801180121000000000000F03F29000000000000F03F380012F6010802125F121E08011000180021C4EDE417C54C21402966465233F602F3BF30013002380D19EC7FD5F20729574021F2B428CF9A9BF53F299C2546BEA05142403103FD3E81252224403978074DF73DE8164049C9184A07FFB1574051A4B7487023A129C01A7708011273121808011801210069370FCD2DEE3F2924CB74E5AD33F73F3809121808021801215F4FFDB1819AE3BF29F25755DE317AF03F380519DAB0EED02D8A584021F4B40312145E8DBF290836261075BE4840315DB38C0795E52240391AA8F53D527727404969E327FF3A8958405141AC152338CBF03F22180801180121000000000000F03F29000000000000F03F38001AF8010803127B121C08011000180021BCDEAA712098E7BF29B7D379D83E1CF83F3001380A121C080210001801211CBE02E356A3BFBF2960DE1FB1F0DA983F30013806195F13CB334FE7584021947AD5F28EECADBF296D5274C9CCCD4840314B9BDD54BACE244039644A14ECA3122340495F13CB334FE75840515CF270589184E43F1A5D080112591218080118012113371FC4C286EA3F298B02309F3CA5F13F38071921876E3E91DE58402113EB09916C4EE03F2946FF1727704549403120E397EF928C23403901F2F6A60BD423404921876E3E91DE58405123DDCC90CE6EF63F22180801180121000000000000F03F29000000000000F03F380022E3020804129501121C08011000180121361A79FEC93B45C029824B88B3FB5631403002380E121A0802100018012120596F21E77536C029795051AABD4F48403802121A08031001180121000000000000000029000000000000F03F380019361A79FEC93B4540214301FB7363A93E4029099398E44679464031804AB5C25AD3424039D8DF997991383240495B4E8C24862146405125B4EA953C8F41C01AAC01080112A7011218080118012174B7ED9E3B44FB3F299AAA17CF2844114038091218080218012132F25A641082F1BF292A0393AC7CDBD8BF3808121808031801217A01F7B7379CE2BF29040DBE74C03AD3BF380512180804180021000000000000000029000000000000F03F380019B273FAA34D4C584021327F65CC1956E63F29781C53133D7849403128F97BD6CB5B2540395C8994FDE97B204049104A8BAFC8E15840517945DC228882FE3F22180801180121000000000000F03F29000000000000F03F3800";
        try {
            System.out.println(JsonFormat.printer().print(Nsrsi.parseFrom(Hex.decodeHex(s))));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }

    }

}
