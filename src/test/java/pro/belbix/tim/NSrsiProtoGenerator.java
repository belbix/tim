package pro.belbix.tim;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import pro.belbix.tim.protobuf.neuron.*;

import static pro.belbix.tim.utils.Common.bytesToHex;

public class NSrsiProtoGenerator {
    private final static boolean create = false;

    public static void main(String[] args) throws InvalidProtocolBufferException {
        Nsrsi nsrsi;
        if (create) {
            nsrsi = create();
        } else {
            Nsrsi.Builder builder = Nsrsi.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(read(), builder);
            nsrsi = builder.build();
        }
        String customHex = bytesToHex(nsrsi.toByteArray());
        try {
            System.out.println(Nsrsi.parseFrom(Hex.decodeHex(customHex)));
        } catch (InvalidProtocolBufferException | DecoderException e) {
            e.printStackTrace();
        }
        System.out.println(customHex);
    }

    private static Nsrsi create() {
        return Nsrsi.newBuilder()
                .setOpenLong(NeuroLinkModel.newBuilder()
                        .setType(NeuroLinkType.LONG_OPEN)
                        .setInput(NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setIndexOfCandle(0)
                                                .setPositive(true)
                                                .setThreshold(1)
                                                .setWeight(1)
                                                .addLinks(1)
                                                .setEpochCount(0)
                                )
                        )
                        .putHidden(1, NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setPositive(true)
                                                .setThreshold(1)
                                                .setWeight(1)
                                                .setEpochCount(0)
                                ).build())
                        .setResult(NeuronModel.newBuilder()
                                .setId(1)
                                .setPositive(true)
                                .setThreshold(1)
                                .setWeight(1)
                                .setEpochCount(0))
                )
                .setCloseLong(NeuroLinkModel.newBuilder()
                        .setType(NeuroLinkType.LONG_CLOSE)
                        .setInput(NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setIndexOfCandle(0)
                                                .setPositive(false)
                                                .setThreshold(-1)
                                                .setWeight(1)
                                                .addLinks(1)
                                                .setEpochCount(0)
                                )
                        )
                        .putHidden(1, NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setPositive(true)
                                                .setThreshold(1)
                                                .setWeight(1)
                                                .setEpochCount(0)
                                ).build())
                        .setResult(NeuronModel.newBuilder()
                                .setId(1)
                                .setPositive(true)
                                .setThreshold(1)
                                .setWeight(1)
                                .setEpochCount(0))
                )
                .setOpenShort(NeuroLinkModel.newBuilder()
                        .setType(NeuroLinkType.SHORT_OPEN)
                        .setInput(NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setIndexOfCandle(0)
                                                .setPositive(false)
                                                .setThreshold(-1)
                                                .setWeight(1)
                                                .addLinks(1)
                                                .setEpochCount(0)
                                )
                        )
                        .putHidden(1, NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setPositive(true)
                                                .setThreshold(1)
                                                .setWeight(1)
                                                .setEpochCount(0)
                                ).build())
                        .setResult(NeuronModel.newBuilder()
                                .setId(1)
                                .setPositive(true)
                                .setThreshold(1)
                                .setWeight(1)
                                .setEpochCount(0))
                )
                .setCloseShort(NeuroLinkModel.newBuilder()
                        .setType(NeuroLinkType.SHORT_CLOSE)
                        .setInput(NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setIndexOfCandle(0)
                                                .setPositive(true)
                                                .setThreshold(1)
                                                .setWeight(1)
                                                .addLinks(1)
                                                .setEpochCount(0)
                                )
                        )
                        .putHidden(1, NeuronLayerModel.newBuilder()
                                .setMutateDoubleMax(100)
                                .setMutateDoubleBase(0.1)
                                .setBaseChanceDouble(50)
                                .setBaseChanceLink(10)
                                .setBaseChanceNeuron(10)
                                .setBaseChanceHidden(1)
                                .setCountBase(100)
                                .setCountMin(1)
                                .addNeurons(
                                        NeuronModel.newBuilder()
                                                .setId(1)
                                                .setPositive(true)
                                                .setThreshold(1)
                                                .setWeight(1)
                                                .setEpochCount(0)
                                ).build())
                        .setResult(NeuronModel.newBuilder()
                                .setId(1)
                                .setPositive(true)
                                .setThreshold(1)
                                .setWeight(1)
                                .setEpochCount(0))
                )
                .build();
    }

    private static String read() {
        return "{\n" +
                "  \"openLong\": {\n" +
                "    \"type\": \"LONG_OPEN\",\n" +
                "    \"input\": {\n" +
                "      \"neurons\": [{\n" +
                "        \"id\": \"1\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": true,\n" +
                "        \"threshold\": -7.445833479408375,\n" +
                "        \"weight\": -5.386651573952538,\n" +
                "        \"links\": [\"1\"],\n" +
                "        \"epochCount\": \"9\"\n" +
                "      }, {\n" +
                "        \"id\": \"2\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": true,\n" +
                "        \"threshold\": 8.401387398593096,\n" +
                "        \"weight\": 10.746116576526228,\n" +
                "        \"links\": [\"1\"],\n" +
                "        \"epochCount\": \"6\"\n" +
                "      }],\n" +
                "      \"mutateDoubleMax\": 98.40564185130815,\n" +
                "      \"mutateDoubleBase\": -5.72745846554732,\n" +
                "      \"baseChanceDouble\": 48.524827711881834,\n" +
                "      \"baseChanceLink\": -25.67051558385353,\n" +
                "      \"baseChanceNeuron\": 23.243484342476272,\n" +
                "      \"countBase\": 95.79324786228085,\n" +
                "      \"countMin\": -4.026662560968612\n" +
                "    },\n" +
                "    \"hidden\": {\n" +
                "      \"1\": {\n" +
                "        \"neurons\": [{\n" +
                "          \"id\": \"1\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": 1.502725373993261,\n" +
                "          \"weight\": 4.011526057859364,\n" +
                "          \"epochCount\": \"8\"\n" +
                "        }],\n" +
                "        \"mutateDoubleMax\": 99.93221902600729,\n" +
                "        \"mutateDoubleBase\": -0.003393999435499851,\n" +
                "        \"baseChanceDouble\": 49.95142565362168,\n" +
                "        \"baseChanceLink\": 8.3001780342537,\n" +
                "        \"baseChanceNeuron\": 10.103216609139794,\n" +
                "        \"countBase\": 99.38786473084556,\n" +
                "        \"countMin\": 0.5467950698659446\n" +
                "      }\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "      \"id\": \"1\",\n" +
                "      \"positive\": true,\n" +
                "      \"threshold\": 1.0,\n" +
                "      \"weight\": 1.0,\n" +
                "      \"epochCount\": \"0\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"closeLong\": {\n" +
                "    \"type\": \"LONG_CLOSE\",\n" +
                "    \"input\": {\n" +
                "      \"neurons\": [{\n" +
                "        \"id\": \"1\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": false,\n" +
                "        \"threshold\": 9.935596329081974,\n" +
                "        \"weight\": -1.1882230763189,\n" +
                "        \"links\": [\"1\", \"2\"],\n" +
                "        \"epochCount\": \"12\"\n" +
                "      }],\n" +
                "      \"mutateDoubleMax\": 92.64111014222698,\n" +
                "      \"mutateDoubleBase\": 2.4189153327069217,\n" +
                "      \"baseChanceDouble\": 47.03395363356612,\n" +
                "      \"baseChanceLink\": 10.06669238943005,\n" +
                "      \"baseChanceNeuron\": 5.726798881603706,\n" +
                "      \"countBase\": 94.78119070278184,\n" +
                "      \"countMin\": -4.31812126570484\n" +
                "    },\n" +
                "    \"hidden\": {\n" +
                "      \"1\": {\n" +
                "        \"neurons\": [{\n" +
                "          \"id\": \"1\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": 0.9681290336937931,\n" +
                "          \"weight\": 1.450117012322786,\n" +
                "          \"epochCount\": \"8\"\n" +
                "        }, {\n" +
                "          \"id\": \"2\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": -0.6126106716496728,\n" +
                "          \"weight\": 1.029832714547862,\n" +
                "          \"epochCount\": \"5\"\n" +
                "        }],\n" +
                "        \"mutateDoubleMax\": 98.15904639539386,\n" +
                "        \"mutateDoubleBase\": -0.01082223071041068,\n" +
                "        \"baseChanceDouble\": 49.48794748178267,\n" +
                "        \"baseChanceLink\": 9.438609467875542,\n" +
                "        \"baseChanceNeuron\": 11.733049331891426,\n" +
                "        \"countBase\": 98.13635127409817,\n" +
                "        \"countMin\": 1.049614083337261\n" +
                "      }\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "      \"id\": \"1\",\n" +
                "      \"positive\": true,\n" +
                "      \"threshold\": 1.0,\n" +
                "      \"weight\": 1.0,\n" +
                "      \"epochCount\": \"0\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"openShort\": {\n" +
                "    \"type\": \"SHORT_OPEN\",\n" +
                "    \"input\": {\n" +
                "      \"neurons\": [{\n" +
                "        \"id\": \"1\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": false,\n" +
                "        \"threshold\": -1.0428400240924751,\n" +
                "        \"weight\": 1.3251547728114714,\n" +
                "        \"links\": [\"1\"],\n" +
                "        \"epochCount\": \"11\"\n" +
                "      }, {\n" +
                "        \"id\": \"2\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": true,\n" +
                "        \"threshold\": 0.19508209672216809,\n" +
                "        \"weight\": 1.4067473928847944,\n" +
                "        \"links\": [\"1\", \"2\"],\n" +
                "        \"epochCount\": \"17\"\n" +
                "      }, {\n" +
                "        \"id\": \"3\",\n" +
                "        \"indexOfCandle\": 1,\n" +
                "        \"positive\": false,\n" +
                "        \"threshold\": 0.294176484984463,\n" +
                "        \"weight\": 1.195901028078566,\n" +
                "        \"links\": [\"1\"],\n" +
                "        \"epochCount\": \"7\"\n" +
                "      }, {\n" +
                "        \"id\": \"4\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": true,\n" +
                "        \"threshold\": 0.002201040570802718,\n" +
                "        \"weight\": 0.7429953831951631,\n" +
                "        \"links\": [\"1\"],\n" +
                "        \"epochCount\": \"6\"\n" +
                "      }],\n" +
                "      \"mutateDoubleMax\": 99.63327125148776,\n" +
                "      \"mutateDoubleBase\": -0.005745934548972487,\n" +
                "      \"baseChanceDouble\": 50.06563201759934,\n" +
                "      \"baseChanceLink\": 10.284057174925373,\n" +
                "      \"baseChanceNeuron\": 10.018690071735929,\n" +
                "      \"countBase\": 99.63327125148776,\n" +
                "      \"countMin\": 0.28715313004828463\n" +
                "    },\n" +
                "    \"hidden\": {\n" +
                "      \"1\": {\n" +
                "        \"neurons\": [{\n" +
                "          \"id\": \"1\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": 1.108402702171479,\n" +
                "          \"weight\": 1.4011232072212134,\n" +
                "          \"epochCount\": \"12\"\n" +
                "        }, {\n" +
                "          \"id\": \"2\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": -0.11646174813091092,\n" +
                "          \"weight\": 0.9700906053459761,\n" +
                "          \"epochCount\": \"1\"\n" +
                "        }],\n" +
                "        \"mutateDoubleMax\": 98.0218752704949,\n" +
                "        \"mutateDoubleBase\": 0.05120503673641616,\n" +
                "        \"baseChanceDouble\": 50.58789657747135,\n" +
                "        \"baseChanceLink\": 8.250250451025106,\n" +
                "        \"baseChanceNeuron\": 9.918083118611522,\n" +
                "        \"countBase\": 97.88551851212068,\n" +
                "        \"countMin\": 2.9121812289695432\n" +
                "      }\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "      \"id\": \"1\",\n" +
                "      \"positive\": true,\n" +
                "      \"threshold\": 1.0,\n" +
                "      \"weight\": 1.0,\n" +
                "      \"epochCount\": \"0\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"closeShort\": {\n" +
                "    \"type\": \"SHORT_CLOSE\",\n" +
                "    \"input\": {\n" +
                "      \"neurons\": [{\n" +
                "        \"id\": \"1\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": true,\n" +
                "        \"threshold\": 0.9037866788813018,\n" +
                "        \"weight\": -5.716523757539755,\n" +
                "        \"links\": [\"1\", \"2\", \"3\"],\n" +
                "        \"epochCount\": \"19\"\n" +
                "      }, {\n" +
                "        \"id\": \"2\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": true,\n" +
                "        \"threshold\": -19.040009217599582,\n" +
                "        \"weight\": 7.204046309244514,\n" +
                "        \"epochCount\": \"9\"\n" +
                "      }, {\n" +
                "        \"id\": \"3\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": false,\n" +
                "        \"threshold\": 14.998687525194507,\n" +
                "        \"weight\": 3.6896643716611206,\n" +
                "        \"epochCount\": \"5\"\n" +
                "      }, {\n" +
                "        \"id\": \"4\",\n" +
                "        \"indexOfCandle\": 0,\n" +
                "        \"positive\": true,\n" +
                "        \"threshold\": -0.4230100955320789,\n" +
                "        \"weight\": 0.9724201014392752,\n" +
                "        \"links\": [\"1\"],\n" +
                "        \"epochCount\": \"2\"\n" +
                "      }],\n" +
                "      \"mutateDoubleMax\": 91.35058681911657,\n" +
                "      \"mutateDoubleBase\": -0.02008750030372032,\n" +
                "      \"baseChanceDouble\": 50.81809508974979,\n" +
                "      \"baseChanceLink\": 11.45752777491702,\n" +
                "      \"baseChanceNeuron\": 19.350446824085772,\n" +
                "      \"countBase\": 91.35058681911657,\n" +
                "      \"countMin\": 7.556798258268187\n" +
                "    },\n" +
                "    \"hidden\": {\n" +
                "      \"1\": {\n" +
                "        \"neurons\": [{\n" +
                "          \"id\": \"1\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": 7.790411227429845,\n" +
                "          \"weight\": 15.917004356525831,\n" +
                "          \"epochCount\": \"16\"\n" +
                "        }, {\n" +
                "          \"id\": \"2\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": -3.3167794295238515,\n" +
                "          \"weight\": 7.4447863222507955,\n" +
                "          \"epochCount\": \"10\"\n" +
                "        }, {\n" +
                "          \"id\": \"3\",\n" +
                "          \"positive\": true,\n" +
                "          \"threshold\": -2.23922120755966,\n" +
                "          \"weight\": 4.04381501338498,\n" +
                "          \"epochCount\": \"11\"\n" +
                "        }],\n" +
                "        \"mutateDoubleMax\": 95.57256022436577,\n" +
                "        \"mutateDoubleBase\": 2.5027232997813758,\n" +
                "        \"baseChanceDouble\": 55.637010458233696,\n" +
                "        \"baseChanceLink\": 12.206233445934595,\n" +
                "        \"baseChanceNeuron\": 10.150920316402022,\n" +
                "        \"countBase\": 95.57256022436577,\n" +
                "        \"countMin\": -1.704476504441996\n" +
                "      }\n" +
                "    },\n" +
                "    \"result\": {\n" +
                "      \"id\": \"1\",\n" +
                "      \"positive\": true,\n" +
                "      \"threshold\": 1.0,\n" +
                "      \"weight\": 1.0,\n" +
                "      \"epochCount\": \"0\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }

}
