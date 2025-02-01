package net.ristovic.benchmarks.osd.serializers;

public class Serializers {

    private Serializers() {
    }

    public static Serializer fromTag(String tag) throws Exception {
        switch (tag) {
            case "java":
                return new JavaSerializer();
            case "jackson-databind":
                return new JacksonSerializer();
            case "gson":
                return new GsonSerializer();
            case "dsljson":
                return new DsljsonSerializer();
            case "fastjson":
                return new FastjsonSerializer();
            case "kryo":
                return new KryoSerializer();
            case "kryo-ref":
                return new KryoSerializer(true);
            case "graalvm-oss":
                return new GraalvmOssSerializer();
            default:
                throw new RuntimeException("unknown serializer");
        }
    } 

}
