package net.ristovic.benchmarks.osd.data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import net.ristovic.benchmarks.osd.data.gen.*;
import net.ristovic.benchmarks.osd.data.model.*;

/* Denotes a value type (int, float, etc), or a more complex data type from a string tag:
 *  - int          - random integer
 *  - str[n]       - random string of size n
 *  - recI[n]      - record with n int fields
 *  - recD[n]      - record with n double fields
 *  - recS[n]      - record with n string fields
 *  - arr<T>[n]    - array of size n where elements are determined by tag T
 *      example: arr<str[4]>[16]
 *  - client       - POJO client
 *  - map<K,V>[n]  - map of size n where keys are determined by tag T and 
 *                   values are determined by tag V
 *      example: map<str[4],client>[16]
 */
public abstract class Data {

    public static Class<?>[] CLASSES = {
        Object[].class,
        String[].class,
        long[].class,
        UUID.class,
        ArrayList.class,
        HashMap.class,
        BigDecimal.class,
        LocalDate.class,
        LocalDateTime.class,
        OffsetDateTime.class,
        Client.class,
        Client.EyeColor.class,
        Client.Partner.class,
        RecInt.One.class,
        RecInt.Two.class,
        RecInt.Three.class,
        RecInt.Four.class,
        RecInt.Five.class,
        RecInt.Six.class,
        RecInt.Seven.class,
        RecInt.Eight.class,
        RecInt.Nine.class,
        RecInt.Ten.class,
        RecInt.Eleven.class,
        RecInt.Twelve.class,
        RecInt.Thirteen.class,
        RecInt.Fourteen.class,
        RecInt.Fifteen.class,
        RecInt.Sixteen.class,
        RecDbl.One.class,
        RecDbl.Two.class,
        RecDbl.Three.class,
        RecDbl.Four.class,
        RecDbl.Five.class,
        RecDbl.Six.class,
        RecDbl.Seven.class,
        RecDbl.Eight.class,
        RecDbl.Nine.class,
        RecDbl.Ten.class,
        RecDbl.Eleven.class,
        RecDbl.Twelve.class,
        RecDbl.Thirteen.class,
        RecDbl.Fourteen.class,
        RecDbl.Fifteen.class,
        RecDbl.Sixteen.class,
        RecStr.One.class,
        RecStr.Two.class,
        RecStr.Three.class,
        RecStr.Four.class,
        RecStr.Five.class,
        RecStr.Six.class,
        RecStr.Seven.class,
        RecStr.Eight.class,
        RecStr.Nine.class,
        RecStr.Ten.class,
        RecStr.Eleven.class,
        RecStr.Twelve.class,
        RecStr.Thirteen.class,
        RecStr.Fourteen.class,
        RecStr.Fifteen.class,
        RecStr.Sixteen.class,
    };

    private static Random rng;
   
    public static void setRng(Random r) {
        rng = r;
    }
    
    public static void debugPrint(Object obj) {
        if (obj instanceof byte[] arr) {
            System.out.println(Arrays.toString(arr));
        } else {
            System.out.println(obj.getClass().isArray() ? Arrays.toString((Object[]) obj) : obj);
        }
    }

    public static Object createDataObject(String tag) {
        assert rng != null : "RNG not setup";
        Data d = parseTag(tag);
        return d.generateObject(rng);
    } 

    public static Data parseTag(String tag) {
        String type = tag;

        int size = parseSizeSpecifier(tag);
        if (size > 0) {
            type = type.substring(0, type.lastIndexOf('['));
        }

        String t = parseTypeSpecifier(type);
        if (t != null) {
            type = type.substring(0, type.indexOf('<'));
        }

        switch (type) {
            case "int":
                return new IntData();
            case "dbl":
                return new DoubleData();
            case "str":
                return new StrData(size);
            case "arr":
                Data arrType = parseTag(t);
                return new ArrData(arrType, size);
            case "lst":
                Data lstType = parseTag(t);
                return new ListData(lstType, size);
            case "map":
                String[] kv = t.split(",");
                Data k = parseTag(kv[0]);
                Data v = parseTag(kv[1]);
                return new MapData(k, v, size);
            case "recI":
                return new RecIntData(size);
            case "recD":
                return new RecDblData(size);
            case "recS":
                return new RecStrData(size);
            case "client":
                return new ClientData();
            default:
                throw new RuntimeException("unknown object type: " + type);
        }
    }

    private static int parseSizeSpecifier(String tag) {
        if (tag.endsWith("]")) {
            return asInt(tag.substring(tag.lastIndexOf('[') + 1, tag.length() - 1));
        } else {
            return 0;
        }
    }

    private static String parseTypeSpecifier(String tag) {
        if (tag.endsWith(">")) {
            return tag.substring(tag.indexOf('<') + 1, tag.length() - 1);
        } else {
            return null;
        }
    }

    abstract Object generateObject(Random rng);


    private static final class IntData extends Data {
        public IntData() {} 

        public Integer generateObject(Random rng) {
            return new ValueDataGenerator(rng).randInt();
        }
    }

    private static final class DoubleData extends Data {
        public DoubleData() {} 

        public Double generateObject(Random rng) {
            return new ValueDataGenerator(rng).randDouble();
        }
    }

    private static final class StrData extends Data {
        private int size;

        public StrData(int size) {
            this.size = size;
        }

        public String generateObject(Random rng) {
            return new ValueDataGenerator(rng).randStr(size);
        }
    }

    private static final class RecIntData extends Data {
        private int fields;

        public RecIntData(int fields) {
            this.fields = fields;
        }

        public Object generateObject(Random rng) {
            return RecInt.withNFields(new ValueDataGenerator(rng), fields);
        }
    }

    private static final class RecDblData extends Data {
        private int fields;

        public RecDblData(int fields) {
            this.fields = fields;
        }

        public Object generateObject(Random rng) {
            return RecDbl.withNFields(new ValueDataGenerator(rng), fields);
        }
    }

    private static final class RecStrData extends Data {
        private int fields;

        public RecStrData(int fields) {
            this.fields = fields;
        }

        public Object generateObject(Random rng) {
            return RecStr.withNFields(new ValueDataGenerator(rng), fields);
        }
    }

    private static final class ArrData extends Data {
        private int size;
        private Data element;

        public ArrData(Data element, int size) {
            this.element = element;
            this.size = size;
        }

        public Object generateObject(Random rng) {
            Object[] objArr = new Object[size];
            for (int i = 0; i < objArr.length; i++) {
                objArr[i] = element.generateObject(rng);
            }
            return objArr;
        }
    }

    private static final class ListData extends Data {
        private int size;
        private Data element;

        public ListData(Data element, int size) {
            this.element = element;
            this.size = size;
        }

        public Object generateObject(Random rng) {
            ArrayList<Object> lst = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                lst.add(element.generateObject(rng));
            }
            return lst;
        }
    }

    private static final class MapData extends Data {
        private int size;
        private Data k;
        private Data v;

        public MapData(Data k, Data v, int size) {
            this.k = k;
            this.v = v;
            this.size = size;
        }

        public Object generateObject(Random rng) {
            HashMap<Object, Object> map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                Object key = k.generateObject(rng);
                Object value = v.generateObject(rng);
                map.put(key, value);
            }
            return map;
        }
    }

    private static final class ClientData extends Data {
        public ClientData() {}

        public Client generateObject(Random rng) {
            Client client = new Client(); 
            new ClientGenerator(rng).generateFields(client);
            return client; 
        }
    }

    private static int asInt(String s) {
        return Integer.parseInt(s);
    }

}
