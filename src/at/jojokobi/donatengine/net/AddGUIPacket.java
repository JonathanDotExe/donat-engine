package at.jojokobi.donatengine.net;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import at.jojokobi.donatengine.gui.GUI;
import at.jojokobi.donatengine.gui.GUISystem;
import at.jojokobi.donatengine.level.Level;
import at.jojokobi.donatengine.level.LevelHandler;
import at.jojokobi.donatengine.objects.GameObject;
import at.jojokobi.donatengine.serialization.SerializationWrapper;

public class AddGUIPacket implements ServerPacket {
	
	public static final ServerPacketType PACKET_TYPE = new ServerPacketType() {
		
		@Override
		public List<ServerPacket> onUpdate(Level level, GameObject object, long id, SerializationWrapper serialization) {
			return new ArrayList<>();
		}
		
		@Override
		public List<ServerPacket> onSpawn(Level level, GameObject object, long id) {
			return Arrays.asList();
		}

		@Override
		public List<ServerPacket> onDelete(Level level, GameObject object, long id) {
			return new ArrayList<>();
		}
		
		@Override
		public List<ServerPacket> recreatePackets(Level level) {
			List<ServerPacket> packets = new ArrayList<>();
			for (Map.Entry<Long, GUI> entry : level.getGUIs().entrySet()) {
				packets.add(new AddGUIPacket(entry.getValue().getType(), entry.getValue().getData(), entry.getKey()));
			}
			return packets;
		}
		
		@Override
		public List<ServerPacket> onAddGUI(GUISystem system, GUI gui, long id) {
			return Arrays.asList(new AddGUIPacket(gui.getType(), gui.getData(), id));
		};
	};
	
	private String gui;
	private Object data;
	private long id;

	public AddGUIPacket(String gui, Object data, long id) {
		super();
		this.gui = gui;
		this.id = id;
	}
	
	public AddGUIPacket() {
		this("", null, 0);
	}

	@Override
	public void serialize(DataOutput buffer, SerializationWrapper serialization) throws IOException {
		buffer.writeUTF(gui);
		serialization.serialize(data, buffer);
		buffer.writeLong(id);
	}

	@Override
	public void deserialize(DataInput buffer, SerializationWrapper serialization) throws IOException {
		gui = buffer.readUTF();
		id = buffer.readLong();
		data = serialization.deserialize(Object.class, buffer);
	}

	@Override
	public void apply(Level level, LevelHandler handler, SerializationWrapper serialization) {
		level.getGuiSystem().showGUI(gui, data, id);
	}

}
