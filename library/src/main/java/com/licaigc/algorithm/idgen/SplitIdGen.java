package com.licaigc.algorithm.idgen;

/**
 * Created by zhaowencong on 16/3/22.
 */
public class SplitIdGen {

    public long ms;
    public long shardId;
    public long seqId;

    public void splitId(long id){
        this.ms=(long)(id>>23) + IdGenConst.epoch;
        this.shardId = (id >> 12) & IdGenConst.shardMask;
        this.seqId = id & IdGenConst.seqMask;
        System.out.println("ms:" + ms + "-shardId:" + shardId + "-seqId:"+seqId);
    }
}
