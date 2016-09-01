package com.licaigc.algorithm.idgen;

/**
 * Created by zhaowencong on 16/3/22.
 */
public class IdGenConst {
    public static final long epoch=1325376000000l; // 2012-01-01 00:00:00 UTC
    public static final long shardMask=(long)(1<<11)-1;
    public static final long seqMask=(long)(1<<12)-1;
}
