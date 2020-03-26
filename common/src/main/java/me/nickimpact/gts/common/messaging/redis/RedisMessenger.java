package me.nickimpact.gts.common.messaging.redis;

import lombok.RequiredArgsConstructor;
import me.nickimpact.gts.api.messaging.IncomingMessageConsumer;
import me.nickimpact.gts.api.messaging.Messenger;
import me.nickimpact.gts.api.messaging.message.OutgoingMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

@RequiredArgsConstructor
public class RedisMessenger implements Messenger {

	private static final String CHANNEL = "gts:update";

	private final GTSPlugin plugin;
	private final IncomingMessageConsumer consumer;

	private JedisPool jedisPool;
	private Subscription sub;

	public void init(String address, String password) {
		String[] addressSplit = address.split(":");
		String host = addressSplit[0];
		int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 6379;

		if(password.equals("")) {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
		} else {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port, 0, password);
		}

		this.plugin.getScheduler().executeAsync(() -> {
			this.sub = new Subscription(this);
			try(Jedis jedis = this.jedisPool.getResource()) {
				jedis.subscribe(this.sub, CHANNEL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void sendOutgoingMessage(@NonNull OutgoingMessage outgoingMessage) {
		try (Jedis jedis = this.jedisPool.getResource()) {
			jedis.publish(CHANNEL, outgoingMessage.asEncodedString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		this.sub.unsubscribe();
		this.jedisPool.destroy();
	}

	@RequiredArgsConstructor
	private static class Subscription extends JedisPubSub {
		private final RedisMessenger parent;

		@Override
		public void onMessage(String channel, String msg) {
			if(!channel.equals(CHANNEL)) {
				return;
			}

			this.parent.consumer.consumeIncomingMessageAsString(msg);
		}
	}
}
