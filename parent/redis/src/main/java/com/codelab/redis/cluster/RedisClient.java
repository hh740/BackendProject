package com.codelab.redis.cluster;

import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.Pool;
import redis.clients.util.Slowlog;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangke on 16/4/23.
 * pack the interface from jedisCluster & jedis
 */
public interface RedisClient {

    public String set(String key, String value);

    public String set(String key, String value, String nxxx, String expx, long time);

    public String get(String key);

    public Long exists(String... keys);

    public Boolean exists(String key);

    public Long del(String... keys);

    public Long del(String key);

    public String type(String key);

    public Set<String> keys(String pattern);

    public String rename(String oldkey, String newkey);

    public Long renamenx(String oldkey, String newkey);

    public Long expire(String key, int seconds);

    public Long expireAt(String key, long unixTime);

    public Long ttl(String key);

    public Long move(String key, int dbIndex);

    public String getSet(String key, String value);

    public List<String> mget(String... keys);

    public Long setnx(String key, String value);

    public String setex(String key, int seconds, String value);

    public String mset(String... keysvalues);

    public Long msetnx(String... keysvalues);

    public Long decrBy(String key, long integer);

    public Long decr(String key);

    public Long incrBy(String key, long integer);

    public Double incrByFloat(String key, double value);

    public Long incr(String key);

    public Long append(String key, String value);

    public String substr(String key, int start, int end);

    public Long hset(String key, String field, String value);

    public String hget(String key, String field);

    public Long hsetnx(String key, String field, String value);

    public String hmset(String key, Map<String, String> hash);

    public List<String> hmget(String key, String... fields);

    public Long hincrBy(String key, String field, long value);

    public Double hincrByFloat(String key, String field, double value);

    public Boolean hexists(String key, String field);

    public Long hdel(String key, String... fields);

    public Long hlen(String key);

    public Set<String> hkeys(String key);

    public List<String> hvals(String key);

    public Map<String, String> hgetAll(String key);

    public Long rpush(String key, String... strings);

    public Long lpush(String key, String... strings);

    public Long llen(String key);

    public List<String> lrange(String key, long start, long end);

    public String ltrim(String key, long start, long end);

    public String lindex(String key, long index);

    public String lset(String key, long index, String value);

    public Long lrem(String key, long count, String value);

    public String lpop(String key);

    public String rpop(String key);

    public String rpoplpush(String srckey, String dstkey);

    public Long sadd(String key, String... members);

    public Set<String> smembers(String key);

    public Long srem(String key, String... members);

    public String spop(String key);

    public Set<String> spop(String key, long count);

    public Long smove(String srckey, String dstkey, String member);

    public Long scard(String key);

    public Boolean sismember(String key, String member);

    public Set<String> sinter(String... keys);

    public Long sinterstore(String dstkey, String... keys);

    public Set<String> sunion(String... keys);

    public Long sunionstore(String dstkey, String... keys);

    public Set<String> sdiff(String... keys);

    public Long sdiffstore(String dstkey, String... keys);

    public String srandmember(String key);

    public List<String> srandmember(String key, int count);

    public Long zadd(String key, double score, String member);

    public Long zadd(String key, double score, String member, ZAddParams params);

    public Long zadd(String key, Map<String, Double> scoreMembers);

    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params);

    public Set<String> zrange(String key, long start, long end);

    public Long zrem(String key, String... members);

    public Double zincrby(String key, double score, String member);

    public Double zincrby(String key, double score, String member, ZIncrByParams params);

    public Long zrank(String key, String member);

    public Long zrevrank(String key, String member);

    public Set<String> zrevrange(String key, long start, long end);

    public Set<Tuple> zrangeWithScores(String key, long start, long end);

    public Set<Tuple> zrevrangeWithScores(String key, long start, long end);

    public Long zcard(String key);

    public Double zscore(String key, String member);

    public List<String> sort(String key);

    public List<String> sort(String key, SortingParams sortingParameters);

    public List<String> blpop(int timeout, String... keys);

    public List<String> blpop(String arg);

    public List<String> brpop(String arg);

    public Long sort(String key, SortingParams sortingParameters, String dstkey);

    public Long sort(String key, String dstkey);

    public List<String> brpop(int timeout, String... keys);

    public Long zcount(String key, double min, double max);

    public Long zcount(String key, String min, String max);

    public Set<String> zrangeByScore(String key, double min, double max);

    public Set<String> zrangeByScore(String key, String min, String max);

    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count);

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max);

    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max);

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count);

    public Set<String> zrevrangeByScore(String key, double max, double min);

    public Set<String> zrevrangeByScore(String key, String max, String min);

    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min);

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count);

    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count);

    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count);

    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min);

    public Long zremrangeByRank(String key, long start, long end);

    public Long zremrangeByScore(String key, double start, double end);

    public Long zremrangeByScore(String key, String start, String end);

    public Long zunionstore(String dstkey, String... sets);

    public Long zunionstore(String dstkey, ZParams params, String... sets);

    public Long zinterstore(String dstkey, String... sets);

    public Long zinterstore(String dstkey, ZParams params, String... sets);

    public Long zlexcount(String key, String min, String max);

    public Set<String> zrangeByLex(String key, String min, String max);

    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count);

    public Set<String> zrevrangeByLex(String key, String max, String min);

    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);

    public Long zremrangeByLex(String key, String min, String max);

    public Long strlen(String key);

    public Long lpushx(String key, String... string);

    public Long persist(String key);

    public Long rpushx(String key, String... string);

    public String echo(String string);

    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value);

    public String brpoplpush(String source, String destination, int timeout);

    public Boolean setbit(String key, long offset, boolean value);

    public Boolean setbit(String key, long offset, String value);

    public Boolean getbit(String key, long offset);

    public Long setrange(String key, long offset, String value);

    public String getrange(String key, long startOffset, long endOffset);

    public Long bitpos(String key, boolean value);

    public Long bitpos(String key, boolean value, BitPosParams params);

    public Object eval(String script, int keyCount, String... params);

    public void subscribe(JedisPubSub jedisPubSub, String... channels);

    public Long publish(String channel, String message);

    public void psubscribe(JedisPubSub jedisPubSub, String... patterns);

    public Object eval(String script, List<String> keys, List<String> args);

    public Object evalsha(String sha1, List<String> keys, List<String> args);

    public Object evalsha(String sha1, int keyCount, String... params);

    public Long bitcount(String key);

    public Long bitcount(String key, long start, long end);

    public Long bitop(BitOP op, String destKey, String... srcKeys);

    public Long pexpire(String key, long milliseconds);

    public Long pexpireAt(String key, long millisecondsTimestamp);

    public Long pttl(String key);

    public String psetex(String key, long milliseconds, String value);

    public String set(String key, String value, String nxxx);

    public ScanResult<Map.Entry<String, String>> hscan(String key, int cursor);

    public ScanResult<String> sscan(String key, int cursor);

    public ScanResult<Tuple> zscan(String key, int cursor);

    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor);

    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params);

    public ScanResult<String> sscan(String key, String cursor);

    public ScanResult<String> sscan(String key, String cursor, ScanParams params);

    public ScanResult<Tuple> zscan(String key, String cursor);

    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params);

    public Long pfadd(String key, String... elements);

    public long pfcount(String key);

    public long pfcount(String... keys);

    public String pfmerge(String destkey, String... sourcekeys);

    public List<String> blpop(int timeout, String key);

    public List<String> brpop(int timeout, String key);

    public Long geoadd(String key, double longitude, double latitude, String member);

    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap);

    public Double geodist(String key, String member1, String member2);

    public Double geodist(String key, String member1, String member2, GeoUnit unit);

    public List<String> geohash(String key, String... members);

    public List<GeoCoordinate> geopos(String key, String... members);

    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit);

    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param);

    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit);

    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param);

    public ScanResult<String> scan(final String cursor, final ScanParams params);

}

