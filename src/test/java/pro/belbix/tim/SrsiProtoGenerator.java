package pro.belbix.tim;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import pro.belbix.tim.protobuf.srsi.DoubleValue;
import pro.belbix.tim.protobuf.srsi.Srsi;
import pro.belbix.tim.protobuf.srsi.SrsiNode;

import static pro.belbix.tim.utils.Common.bytesToHex;

public class SrsiProtoGenerator {

    public static void main(String[] args) throws InvalidProtocolBufferException {
//        Srsi srsi = createSrsi();
        Srsi.Builder builder = Srsi.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(read(), builder);
        Srsi srsi = builder.build();
        String customHex = bytesToHex(srsi.toByteArray());
        try {
            System.out.println(Srsi.parseFrom(Hex.decodeHex(customHex)));
        } catch (InvalidProtocolBufferException | DecoderException e) {
            e.printStackTrace();
        }
        System.out.println(customHex);
    }

    private static DoubleValue doubleValue(double v) {
        return DoubleValue.newBuilder().setCount(0).setValue(v).build();
    }


    private static Srsi createSrsi() {
        return Srsi.newBuilder().addNodesLongOpen(
                //------------- LONG OPEN -------------------
                SrsiNode.newBuilder()
                        .setIndex(0)
                        .setSrsiDiffMin(doubleValue(3.7))
                        .setSrsiDiffMax(doubleValue(10.5))
                        .setSrsiMin(doubleValue(9.6))
                        .setSrsiMax(doubleValue(96))
                        .setPriceDiffMin(doubleValue(-12))
                        .setPriceDiffMax(doubleValue(31))
        ).addNodesLongOpen(
                SrsiNode.newBuilder()
                        .setIndex(1)
                        .setSrsiDiffMin(doubleValue(-13))
                        .setSrsiDiffMax(doubleValue(1.15))
                        .setSrsiMin(doubleValue(-12))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(-7))
                        .setPriceDiffMax(doubleValue(23))
        ).addNodesLongOpen(
                SrsiNode.newBuilder()
                        .setIndex(2)
                        .setSrsiDiffMin(doubleValue(-14))
                        .setSrsiDiffMax(doubleValue(1.47))
                        .setSrsiMin(doubleValue(18))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(8.6))
                        .setPriceDiffMax(doubleValue(19.8))
                //------------- LONG CLOSE -------------------
        ).addNodesLongClose(
                SrsiNode.newBuilder()
                        .setIndex(0)
                        .setSrsiDiffMin(doubleValue(-26))
                        .setSrsiDiffMax(doubleValue(-5.28))
                        .setSrsiMin(doubleValue(3.57))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(-0.64))
                        .setPriceDiffMax(doubleValue(12.54))
        ).addNodesLongClose(
                SrsiNode.newBuilder()
                        .setIndex(1)
                        .setSrsiDiffMin(doubleValue(-31.95))
                        .setSrsiDiffMax(doubleValue(20))
                        .setSrsiMin(doubleValue(19.45))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(-13.15))
                        .setPriceDiffMax(doubleValue(22))
        ).addNodesLongClose(
                SrsiNode.newBuilder()
                        .setIndex(2)
                        .setSrsiDiffMin(doubleValue(-42.59))
                        .setSrsiDiffMax(doubleValue(39.7))
                        .setSrsiMin(doubleValue(3))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(-0.3))
                        .setPriceDiffMax(doubleValue(4.75))
                //------------- SHORT OPEN -------------------
        ).addNodesShortOpen(
                SrsiNode.newBuilder()
                        .setIndex(0)
                        .setSrsiDiffMin(doubleValue(-10))
                        .setSrsiDiffMax(doubleValue(-5))
                        .setSrsiMin(doubleValue(0))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(0))
                        .setPriceDiffMax(doubleValue(20))
        ).addNodesShortOpen(
                SrsiNode.newBuilder()
                        .setIndex(1)
                        .setSrsiDiffMin(doubleValue(-25))
                        .setSrsiDiffMax(doubleValue(25))
                        .setSrsiMin(doubleValue(0))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(0))
                        .setPriceDiffMax(doubleValue(20))
        ).addNodesShortOpen(
                SrsiNode.newBuilder()
                        .setIndex(2)
                        .setSrsiDiffMin(doubleValue(-50))
                        .setSrsiDiffMax(doubleValue(50))
                        .setSrsiMin(doubleValue(0))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(0))
                        .setPriceDiffMax(doubleValue(20))
                //------------- SHORT CLOSE -------------------
        ).addNodesShortClose(
                SrsiNode.newBuilder()
                        .setIndex(0)
                        .setSrsiDiffMin(doubleValue(5))
                        .setSrsiDiffMax(doubleValue(10))
                        .setSrsiMin(doubleValue(0))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(0))
                        .setPriceDiffMax(doubleValue(20))
        ).addNodesShortClose(
                SrsiNode.newBuilder()
                        .setIndex(1)
                        .setSrsiDiffMin(doubleValue(-10))
                        .setSrsiDiffMax(doubleValue(5))
                        .setSrsiMin(doubleValue(0))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(0))
                        .setPriceDiffMax(doubleValue(20))
        ).addNodesShortClose(
                SrsiNode.newBuilder()
                        .setIndex(2)
                        .setSrsiDiffMin(doubleValue(-10))
                        .setSrsiDiffMax(doubleValue(5))
                        .setSrsiMin(doubleValue(0))
                        .setSrsiMax(doubleValue(100))
                        .setPriceDiffMin(doubleValue(0))
                        .setPriceDiffMax(doubleValue(20))
        )
                .build();
    }

    private static String read() {
        return "{\n" +
                "  \"nodesLongOpen\": [{\n" +
                "    \"index\": 0,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -11.350596518973486,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 11.48595650409439,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": 30.866463023851303,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 75.58901881944105,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -1.9367367627515304,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 15.351947266083974,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 1,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -27.61925894410692,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 20.71104795667395,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": -18.743216969326685,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 88.20314329903876,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -12.065042606575458,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 6.883498448648572,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 2,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -30.088313754448226,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": -2.0079554345346615,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": 21.7430525334792,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 94.13327146190203,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": 7.093402915180607,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 41.08893619411561,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }],\n" +
                "  \"nodesLongClose\": [{\n" +
                "    \"index\": 0,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -23.87613408815942,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": -12.434833382797086,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": 12.87935554674375,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 93.49748995741521,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -14.431195822190142,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 34.51771041413043,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 1,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -10.017272484673443,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 31.03471421018652,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": 20.08599677480373,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 99.78574725534423,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -16.62889736193551,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 7.961211910892264,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 2,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -37.911778718322985,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 28.45553973301397,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": -9.591226891263602,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 94.93840394016165,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": 1.421070770209608,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": -30.62894389113193,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }],\n" +
                "  \"nodesShortOpen\": [{\n" +
                "    \"index\": 0,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": 1.8686441931105398,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 20.70258796084319,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": -7.183982734701152,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 92.057217809943,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -16.16248476749056,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 45.3561091331355,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 1,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -7.160757125334043,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 24.654028439621886,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": 1.0526857276905979,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 97.4627912690678,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -8.78914379810967,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": -2.1739620541921445,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 2,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -65.18839076048414,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 57.89557269287825,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": -1.8494705877131468,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 92.26377106396531,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -26.104243780098333,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 22.270441220994414,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }],\n" +
                "  \"nodesShortClose\": [{\n" +
                "    \"index\": 0,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -1.0931791563843183,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 10.9778081351163,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": -11.873754393613455,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 81.26430080155603,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -7.604757720942469,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 34.5713119204133,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 1,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -34.67603397274775,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 12.4405997113674,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": 20.0408857296024,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 99.78014961661054,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": -38.878909498539635,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 14.691708250440607,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }, {\n" +
                "    \"index\": 2,\n" +
                "    \"srsiDiffMin\": {\n" +
                "      \"value\": -39.49188347161696,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiDiffMax\": {\n" +
                "      \"value\": 6.513002835994673,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMin\": {\n" +
                "      \"value\": 21.50311034515087,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"srsiMax\": {\n" +
                "      \"value\": 64.14810321080313,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMin\": {\n" +
                "      \"value\": 9.015477023874185,\n" +
                "      \"count\": 1\n" +
                "    },\n" +
                "    \"priceDiffMax\": {\n" +
                "      \"value\": 40.262066909761764,\n" +
                "      \"count\": 1\n" +
                "    }\n" +
                "  }]\n" +
                "}";
    }

}
