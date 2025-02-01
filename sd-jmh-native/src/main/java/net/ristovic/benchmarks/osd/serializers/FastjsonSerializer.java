package net.ristovic.benchmarks.osd.serializers;

import com.alibaba.fastjson.*;

class FastjsonSerializer implements Serializer {

    @Override
    public Object serialize(Object obj) throws Exception {
        return JSON.toJSONString(obj);
    }

    @Override
    public Object deserialize(Class<?> clazz, Object data) throws Exception {
        return JSON.parseObject((String) data, clazz);
    }

}
