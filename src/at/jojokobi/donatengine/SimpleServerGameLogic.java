package at.jojokobi.donatengine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import at.jojokobi.donatengine.audio.AudioSystem;
import at.jojokobi.donatengine.audio.AudioSystemSupplier;
import at.jojokobi.donatengine.input.Input;
import at.jojokobi.donatengine.input.InputHandler;
import at.jojokobi.donatengine.input.SimpleInput;
import at.jojokobi.donatengine.level.Level;
import at.jojokobi.donatengine.level.LevelHandler;
import at.jojokobi.donatengine.net.ClientPacket;
import at.jojokobi.donatengine.objects.Camera;
import at.jojokobi.donatengine.presence.GamePresenceHandler;
import at.jojokobi.donatengine.rendering.RenderData;
import at.jojokobi.donatengine.ressources.IRessourceHandler;
import at.jojokobi.donatengine.serialization.BinarySerializable;
import at.jojokobi.donatengine.serialization.BinarySerialization;
import at.jojokobi.donatengine.serialization.BinarySerializationWrapper;
import at.jojokobi.donatengine.serialization.SerializationWrapper;
import at.jojokobi.netutil.server.Server;
import at.jojokobi.netutil.server.ServerController;

public class SimpleServerGameLogic implements GameLogic {

	private Level level;
	private Server server;
	private Map<Long, Input> inputs = new HashMap<>();
	private SerializationWrapper serialization;

	public SimpleServerGameLogic(Level level, Server server) {
		super();
		this.level = level;
		this.server = server;
		serialization = new BinarySerializationWrapper(BinarySerialization.getInstance().getIdClassFactory());
	}

	@Override
	public void start(InputHandler input, Game game) {
		server.setController(new ServerController() {
			@Override
			public void onConnect(long client, OutputStream out) throws IOException {
//				List<BinarySerializable> packets = level.getBehavior().recreateLevelPackets(level);
//				DataOutputStream data = new DataOutputStream(out);
//				for (BinarySerializable packet : packets) {
//					BinarySerialization.getInstance().serialize(packet, data);
//				}
//				data.flush();
			}

			@Override
			public void listenTo(long client, InputStream in) {

			}
		});
		server.start();

		level.clear();
		level.start(camera, new LevelHandler() {
			
			@Override
			public AudioSystem getAudioSystem(long clientId) {
				return audioSystemSupplier.getAudioSystem(clientId);
			}
			
			@Override
			public Input getInput(long clientId) {
				return input;
			}
			
			@Override
			public void changeLogic(GameLogic logic) {
				logicSwitcher.accept(logic);
			}

			@Override
			public IRessourceHandler getRessourceHandler() {
				return ressourceHandler;
			}
			
			@Override
			public void stop() {
				SimpleServerGameLogic.this.stop();
			}

			@Override
			public SerializationWrapper getSerialization() {
				return serialization;
			}
			
			@Override
			public GamePresenceHandler getGamePresenceHandler() {
				return gamePresenceHandler;
			}
		});
	}

	@Override
	public void update(double delta, InputHandler input, Game game) {
		LevelHandler handler = new LevelHandler() {

			@Override
			public AudioSystem getAudioSystem(long clientId) {
				return audioSystemSupplier.getAudioSystem(clientId);
			}

			@Override
			public Input getInput(long clientId) {
				return clientId == InputHandler.SCENE_INPUT ? input : inputs.get(clientId);
			}

			@Override
			public void changeLogic(GameLogic logic) {
				logicSwitcher.accept(logic);
			}

			@Override
			public IRessourceHandler getRessourceHandler() {
				return ressourceHandler;
			}

			@Override
			public void stop() {
				SimpleServerGameLogic.this.stop();
			}
			
			@Override
			public SerializationWrapper getSerialization() {
				return serialization;
			}
			
			@Override
			public GamePresenceHandler getGamePresenceHandler() {
				return gamePresenceHandler;
			}
		};
		level.update(delta, handler, camera);
		// Update
//		server.update();
//		for (long client : server.fetchRemovedClients()) {
//			level.disconnectPlayer(client);
//		}
		// New Clients
		List<Long> newClients = server.fetchNewClients();
		//Inputs and players
		for (long client : newClients) {
			inputs.put(client, new SimpleInput());
			level.spawnPlayer(client, camera);
		}
		// Recieve packets
		for (long client : server.getClients()) {
			DataInputStream data = new DataInputStream(server.getInputStream(client));
			try {
				while (data.available() > 0) {
					ClientPacket packet = serialization.deserialize(ClientPacket.class, data);
					packet.apply(level, handler, client);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		// Send packets
		try (DataOutputStream out = new DataOutputStream(server.getBroadcastOutputStream())) {
			for (BinarySerializable packet : level.getBehavior().fetchPackets()) {
				serialization.serialize(packet, out);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		// New clients
		for (long client : newClients) {
			List<BinarySerializable> packets = level.getBehavior().recreateLevelPackets(level);
			try (DataOutputStream data = new DataOutputStream(server.getOutputStream(client))) {
				for (BinarySerializable packet : packets) {
					serialization.serialize(packet, data);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void stop(InputHandler input, Game game) {
		level.end();
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(List<RenderData> data) {
		level.render(data, camera, false);
	}

}
