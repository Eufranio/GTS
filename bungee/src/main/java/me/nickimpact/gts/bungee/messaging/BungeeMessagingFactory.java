package me.nickimpact.gts.bungee.messaging;

import me.nickimpact.gts.api.messaging.IncomingMessageConsumer;
import me.nickimpact.gts.api.messaging.Messenger;
import me.nickimpact.gts.api.messaging.MessengerProvider;
import me.nickimpact.gts.bungee.GTSBungeePlugin;
import me.nickimpact.gts.common.messaging.GTSMessagingService;
import me.nickimpact.gts.common.messaging.InternalMessagingService;
import me.nickimpact.gts.common.messaging.MessagingFactory;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BungeeMessagingFactory extends MessagingFactory<GTSBungeePlugin> {

	public BungeeMessagingFactory(GTSBungeePlugin plugin) {
		super(plugin);
	}

	@Override
	protected InternalMessagingService getServiceFor(String messageType) {
		if(messageType.equalsIgnoreCase("pluginmsg") || messageType.equalsIgnoreCase("bungee")) {
			try {
				return new GTSMessagingService(this.getPlugin(), new PluginMessageMessengerProvider());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(messageType.equalsIgnoreCase("redisbungee")) {
			if(this.getPlugin().getBootstrap().getProxy().getPluginManager().getPlugin("RedisBungee") == null) {
				this.getPlugin().getPluginLogger().warn("RedisBungee plugin is not present");
			} else {
				try {
					return new GTSMessagingService(this.getPlugin(), new RedisBungeeMessengerProvider());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return super.getServiceFor(messageType);
	}

	private class PluginMessageMessengerProvider implements MessengerProvider {

		@Override
		public @NonNull String getName() {
			return "PluginMessage";
		}

		@Override
		public @NonNull Messenger obtain(@NonNull IncomingMessageConsumer incomingMessageConsumer) {
			PluginMessageMessenger messenger = new PluginMessageMessenger(getPlugin(), incomingMessageConsumer);
			messenger.init();
			return messenger;
		}

	}

	private class RedisBungeeMessengerProvider implements MessengerProvider {

		@Override
		public @NonNull String getName() {
			return "RedisBungee";
		}

		@Override
		public @NonNull Messenger obtain(@NonNull IncomingMessageConsumer incomingMessageConsumer) {
			RedisBungeeMessenger messenger = new RedisBungeeMessenger(getPlugin(), incomingMessageConsumer);
			messenger.init();
			return messenger;
		}

	}
}
