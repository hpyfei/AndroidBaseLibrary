package com.licaigc.algorithm.idgen;

/**
 * Created by zhaowencong on 16/3/22.
 */
public class NewIdGen {


    private long seqId;
    private long shardId;

    public NewIdGen(long shardId, long seqId) {
        this.shardId = shardId % (IdGenConst.shardMask + 1);
        this.seqId = seqId;

    }

    public long nextWithTime(long time) {
        long id = time - IdGenConst.epoch;
        id <<= 23;
        id |= (this.shardId << 12);
        id |= seqId % (IdGenConst.seqMask + 1);
        System.out.println("id:" + id);
        return id;
    }

    public long next() {
        return nextWithTime(System.currentTimeMillis());
    }

    /**
     * 因为timi中会涉及到用户切换之类的操作,保存一个单例对象维护起来会比较麻烦,所以...
     * <p/>
     * 就不单例了...
     *
     * @param shardId timi中传userId
     * @param time 当前时间戳 单位ms
     * @return
     */
    public static long genNewId(long shardId, long time) {
        long seqId = (long) (Math.random() * Math.pow(2, 12));
        return new NewIdGen(shardId, seqId).nextWithTime(time);
    }


    public static void main(String args[]) {
        new SplitIdGen().splitId(new NewIdGen(11, 22).next());
    }


}
