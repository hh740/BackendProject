package com.miot.redis.cluster;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.JedisClusterCRC16;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RedisClusterClient implements RedisClient {

    private JedisCluster delegate;

    public void setDelegate(JedisCluster jedisCluster) {
        this.delegate = jedisCluster;
    }

    public JedisCluster getDelegate() {
        return delegate;
    }

    public RedisClusterClient(JedisCluster jedisCluster) {
        if (jedisCluster == null) throw new NullPointerException();
        this.delegate = jedisCluster;
    }

    //=========================jedisClent==================================
    @Override
    public String set(String key, String value) {
        return delegate.set(key, value);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        return delegate.set(key, value, nxxx, expx, time);

    }

    @Override
    public String get(String key) {
        return delegate.get(key);
    }

    @Override
    public Boolean exists(String key) {
        return delegate.exists(key);
    }

    @Override
    public Long exists(String... keys) {
        return delegate.exists(keys);
    }

    @Override
    public Long persist(String key) {
        return delegate.persist(key);
    }

    @Override
    public String type(String key) {
        return delegate.type(key);
    }

    @Override
    @Deprecated
    public Set<String> keys(String pattern) {
        return null;
    }

    @Override
    public Long expire(String key, int seconds) {
        return delegate.expire(key, seconds);
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return delegate.pexpire(key, milliseconds);
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return delegate.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return delegate.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Long ttl(String key) {
        return delegate.ttl(key);
    }

    @Override
    public Long pttl(String key) {
        return delegate.pttl(key);
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        return delegate.setbit(key, offset, value);
    }

    @Override
    public Boolean setbit(String key, long offset, String value) {
        return delegate.setbit(key, offset, value);
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return delegate.getbit(key, offset);
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return delegate.setrange(key, offset, value);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return delegate.getrange(key, startOffset, endOffset);
    }

    @Override
    public String getSet(String key, String value) {
        return delegate.getSet(key, value);
    }

    @Override
    public Long setnx(String key, String value) {
        return delegate.setnx(key, value);
    }

    @Override
    public String setex(String key, int seconds, String value) {
        return delegate.setex(key, seconds, value);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return delegate.psetex(key, milliseconds, value);
    }

    @Override
    public Long decrBy(String key, long integer) {
        return delegate.decrBy(key, integer);
    }

    @Override
    public Long decr(String key) {
        return delegate.decr(key);
    }

    @Override
    public Long incrBy(String key, long integer) {
        return delegate.incrBy(key, integer);
    }

    @Override
    public Double incrByFloat(String key, double value) {
        return delegate.incrByFloat(key, value);
    }

    @Override
    public Long incr(String key) {
        return delegate.incr(key);
    }

    @Override
    public Long append(String key, String value) {
        return delegate.append(key, value);
    }

    @Override
    public String substr(String key, int start, int end) {
        return delegate.substr(key, start, end);
    }

    @Override
    public Long hset(String key, String field, String value) {
        return delegate.hset(key, field, value);
    }

    @Override
    public String hget(String key, String field) {
        return delegate.hget(key, field);
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return delegate.hsetnx(key, field, value);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return delegate.hmset(key, hash);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return delegate.hmget(key, fields);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return delegate.hincrBy(key, field, value);
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return delegate.hincrByFloat(key, field, value);
    }

    @Override
    public Boolean hexists(String key, String field) {
        return delegate.hexists(key, field);
    }

    @Override
    public Long hdel(String key, String... field) {
        return delegate.hdel(key, field);
    }

    @Override
    public Long hlen(String key) {
        return delegate.hlen(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return delegate.hkeys(key);
    }

    @Override
    public List<String> hvals(String key) {
        return delegate.hvals(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return delegate.hgetAll(key);
    }

    @Override
    public Long rpush(String key, String... string) {
        return delegate.rpush(key, string);
    }

    @Override
    public Long lpush(String key, String... string) {
        return delegate.lpush(key, string);
    }

    @Override
    public Long llen(String key) {
        return delegate.llen(key);
    }

    @Override
    public List<String> lrange(String key, long start, long end) {
        return delegate.lrange(key, start, end);
    }

    @Override
    public String ltrim(String key, long start, long end) {
        return delegate.ltrim(key, start, end);
    }

    @Override
    public String lindex(String key, long index) {
        return delegate.lindex(key, index);
    }

    @Override
    public String lset(String key, long index, String value) {
        return delegate.lset(key, index, value);
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return delegate.lrem(key, count, value);
    }

    @Override
    public String lpop(String key) {
        return delegate.lpop(key);
    }

    @Override
    public String rpop(String key) {
        return delegate.rpop(key);
    }

    @Override
    public Long sadd(String key, String... member) {
        return delegate.sadd(key, member);
    }

    @Override
    public Set<String> smembers(String key) {
        return delegate.smembers(key);
    }

    @Override
    public Long srem(String key, String... member) {
        return delegate.srem(key, member);
    }

    @Override
    public String spop(String key) {
        return delegate.spop(key);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return delegate.spop(key, count);
    }

    @Override
    public Long scard(String key) {
        return delegate.scard(key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return delegate.sismember(key, member);
    }

    @Override
    public String srandmember(String key) {
        return delegate.srandmember(key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return delegate.srandmember(key, count);
    }

    @Override
    public Long strlen(String key) {
        return delegate.strlen(key);
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return delegate.zadd(key, score, member);
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        return delegate.zadd(key, score, member, params);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return delegate.zadd(key, scoreMembers);
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return delegate.zadd(key, scoreMembers, params);
    }

    @Override
    public Set<String> zrange(String key, long start, long end) {
        return delegate.zrange(key, start, end);
    }

    @Override
    public Long zrem(String key, String... member) {
        return delegate.zrem(key, member);
    }

    @Override
    public Double zincrby(String key, double score, String member) {
        return delegate.zincrby(key, score, member);
    }

    @Override
    public Double zincrby(String key, double score, String member, ZIncrByParams params) {
        return delegate.zincrby(key, score, member, params);
    }

    @Override
    public Long zrank(String key, String member) {
        return delegate.zrank(key, member);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return delegate.zrevrank(key, member);
    }

    @Override
    public Set<String> zrevrange(String key, long start, long end) {
        return delegate.zrevrange(key, start, end);
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        return delegate.zrangeWithScores(key, start, end);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        return delegate.zrevrangeWithScores(key, start, end);
    }

    @Override
    public Long zcard(String key) {
        return delegate.zcard(key);
    }

    @Override
    public Double zscore(String key, String member) {
        return delegate.zscore(key, member);
    }

    @Override
    public List<String> sort(String key) {
        return delegate.sort(key);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return delegate.sort(key, sortingParameters);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return delegate.zcount(key, min, max);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return delegate.zcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return delegate.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return delegate.zrangeByScore(key, min, max);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return delegate.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return delegate.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return delegate.zrevrangeByScore(key, max, min);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return delegate.zrangeByScore(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return delegate.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return delegate.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return delegate.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return delegate.zrevrangeByScore(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return delegate.zrangeByScoreWithScores(key, min, max);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return delegate.zrevrangeByScoreWithScores(key, max, min);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return delegate.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return delegate.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByRank(String key, long start, long end) {
        return delegate.zremrangeByRank(key, start, end);
    }

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        return delegate.zremrangeByScore(key, start, end);
    }

    @Override
    public Long zremrangeByScore(String key, String start, String end) {
        return delegate.zremrangeByScore(key, start, end);
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return delegate.zlexcount(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return delegate.zrangeByLex(key, min, max);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return delegate.zrangeByLex(key, min, max, offset, count);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return delegate.zrevrangeByLex(key, max, min);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return delegate.zrevrangeByLex(key, max, min, offset, count);
    }

    @Override
    public Long zremrangeByLex(String key, String min, String max) {
        return delegate.zremrangeByLex(key, min, max);
    }

    @Override
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        return delegate.linsert(key, where, pivot, value);
    }

    @Override
    public Long lpushx(String key, String... string) {
        return delegate.lpushx(key, string);
    }

    @Override
    public Long rpushx(String key, String... string) {
        return delegate.rpushx(key, string);
    }

    @Override
    public Long del(String key) {
        return delegate.del(key);
    }

    @Override
    public String echo(String string) {
        return delegate.echo(string);
    }

    @Override
    public Long bitcount(String key) {
        return delegate.bitcount(key);
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        return delegate.bitcount(key, start, end);
    }

    @Override
    public Long bitpos(String key, boolean value) {
        return delegate.bitpos(key, value);
    }

    @Override
    public Long bitpos(String key, boolean value, BitPosParams params) {
        return delegate.bitpos(key, value, params);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return delegate.hscan(key, cursor);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return delegate.hscan(key, cursor, params);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        return delegate.sscan(key, cursor);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return delegate.sscan(key, cursor, params);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return delegate.zscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return delegate.zscan(key, cursor, params);
    }

    @Override
    public Long pfadd(String key, String... elements) {
        return delegate.pfadd(key, elements);
    }

    @Override
    public long pfcount(String key) {
        return delegate.pfcount(key);
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return delegate.blpop(timeout, key);
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        return delegate.brpop(timeout, key);
    }

    @Override
    public Long del(String... keys) {
        return delegate.del(keys);
    }

    @Override
    public List<String> blpop(int timeout, String... keys) {
        return delegate.blpop(timeout, keys);
    }

    @Override
    public List<String> brpop(int timeout, String... keys) {
        return delegate.brpop(timeout, keys);
    }

    @Override
    public List<String> mget(String... keys) {
        return delegate.mget(keys);
    }

    @Override
    public String mset(String... keysvalues) {
        return delegate.mset(keysvalues);
    }

    @Override
    public Long msetnx(String... keysvalues) {
        return delegate.msetnx(keysvalues);
    }

    @Override
    public String rename(String oldkey, String newkey) {
        return delegate.rename(oldkey, newkey);
    }

    @Override
    public Long renamenx(String oldkey, String newkey) {
        return delegate.renamenx(oldkey, newkey);
    }

    @Override
    public String rpoplpush(String srckey, String dstkey) {
        return delegate.rpoplpush(srckey, dstkey);
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return delegate.sdiff(keys);
    }

    @Override
    public Long sdiffstore(String dstkey, String... keys) {
        return delegate.sdiffstore(dstkey, keys);
    }

    @Override
    public Set<String> sinter(String... keys) {
        return delegate.sinter(keys);
    }

    @Override
    public Long sinterstore(String dstkey, String... keys) {
        return delegate.sinterstore(dstkey, keys);
    }

    @Override
    public Long smove(String srckey, String dstkey, String member) {
        return delegate.smove(srckey, dstkey, member);
    }

    @Override
    public Long sort(String key, SortingParams sortingParameters, String dstkey) {
        return delegate.sort(key, sortingParameters, dstkey);
    }

    @Override
    public Long sort(String key, String dstkey) {
        return delegate.sort(key, dstkey);
    }

    @Override
    public Set<String> sunion(String... keys) {
        return delegate.sunion(keys);
    }

    @Override
    public Long sunionstore(String dstkey, String... keys) {
        return delegate.sunionstore(dstkey, keys);
    }

    @Override
    public Long zinterstore(String dstkey, String... sets) {
        return delegate.zinterstore(dstkey, sets);
    }

    @Override
    public Long zinterstore(String dstkey, ZParams params, String... sets) {
        return delegate.zinterstore(dstkey, params, sets);
    }

    @Override
    public Long zunionstore(String dstkey, String... sets) {
        return delegate.zunionstore(dstkey, sets);
    }

    @Override
    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        return delegate.zunionstore(dstkey, params, sets);
    }

    @Override
    public String brpoplpush(String source, String destination, int timeout) {
        return delegate.brpoplpush(source, destination, timeout);
    }

    @Override
    public Long publish(String channel, String message) {
        return delegate.publish(channel, message);
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        delegate.subscribe(jedisPubSub, channels);
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        delegate.psubscribe(jedisPubSub, patterns);
    }

    @Override
    public Long bitop(BitOP op, String destKey, String... srcKeys) {
        return delegate.bitop(op, destKey, srcKeys);
    }

    @Override
    public String pfmerge(String destkey, String... sourcekeys) {
        return delegate.pfmerge(destkey, sourcekeys);
    }

    @Override
    public long pfcount(String... keys) {
        return delegate.pfcount(keys);
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        return delegate.eval(script, keyCount, params);
    }

    public Object eval(String script, String key) {
        return delegate.eval(script, key);
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return delegate.eval(script, keys, args);
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String... params) {
        return delegate.evalsha(sha1, keyCount, params);
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return delegate.evalsha(sha1, keys, args);
    }

    public Object evalsha(String script, String key) {
        return delegate.evalsha(script, key);
    }

    public Boolean scriptExists(String sha1, String key) {
        return delegate.scriptExists(sha1, key);
    }

    public List<Boolean> scriptExists(String key, String... sha1) {
        return delegate.scriptExists(key, sha1);
    }

    public String scriptLoad(String script, String key) {
        return delegate.scriptLoad(script, key);
    }

    @Override
    public String set(String key, String value, String nxxx) {
        return delegate.set(key, value, nxxx);
    }

    @Override
    public List<String> blpop(String arg) {
        return delegate.blpop(arg);
    }

    @Override
    public List<String> brpop(String arg) {
        return delegate.brpop(arg);
    }

    @Override
    public Long move(String key, int dbIndex) {
        return delegate.move(key, dbIndex);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, int cursor) {
        return delegate.hscan(key, cursor);
    }

    @Override
    public ScanResult<String> sscan(String key, int cursor) {
        return delegate.sscan(key, cursor);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, int cursor) {
        return delegate.zscan(key, cursor);
    }

    @Override
    public Long geoadd(String key, double longitude, double latitude, String member) {
        return delegate.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return delegate.geoadd(key, memberCoordinateMap);
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        return delegate.geodist(key, member1, member2);
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return delegate.geodist(key, member1, member2, unit);
    }

    @Override
    public List<String> geohash(String key, String... members) {
        return delegate.geohash(key, members);
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        return delegate.geopos(key, members);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return delegate.georadius(key, longitude, latitude, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return delegate.georadius(key, longitude, latitude, radius, unit, param);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return delegate.georadiusByMember(key, member, radius, unit);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return delegate.georadiusByMember(key, member, radius, unit, param);
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        return delegate.scan(cursor,params);
    }

    public void close() throws IOException {
        delegate.close();
    }

    public Map<String, JedisPool> getClusterNodes() {
        return delegate.getClusterNodes();
    }

}
