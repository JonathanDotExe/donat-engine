package at.jojokobi.donatengine.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.jojokobi.donatengine.gui.actions.GUIAction;
import at.jojokobi.donatengine.level.Level;
import at.jojokobi.donatengine.level.LevelHandler;
import at.jojokobi.donatengine.objects.Camera;
import at.jojokobi.donatengine.util.KeyedContainer;
import at.jojokobi.donatengine.util.KeyedHashContainer;
import at.jojokobi.donatengine.util.LongKeySupplier;
import at.jojokobi.donatengine.util.Pair;
import javafx.scene.canvas.GraphicsContext;

public class SimpleGUISystem implements GUISystem {
	
	private List<Listener> listeners = new ArrayList<>();
	private KeyedContainer<Long, GUI> guis = new KeyedHashContainer<>(new LongKeySupplier());
	private GUIFactory factory;
	

	public SimpleGUISystem(GUIFactory factory) {
		super();
		this.factory = factory;
	}

	@Override
	public void removeGUI(long id) {
		GUI gui = getGUI(id);
		guis.remove(id);
		if (gui != null) {
			listeners.forEach(l -> l.onRemoveGUI(this, gui, id));
		}
	}

	@Override
	public Long getID(GUI gui) {
		return guis.getKey(gui);
	}

	@Override
	public void render(GraphicsContext ctx, double width, double height) {
		for (Long id : guis.keySet()) {
			guis.get(id).render(ctx, width, height);
		}
	}

	@Override
	public List<Pair<Long, GUIAction>> update(Level level, double width, double height, LevelHandler handler, Camera camera, double delta) {
		List<Pair<Long, GUIAction>> actions = new ArrayList<>();
		for (Long id : guis.keySet()) {
			GUI gui = guis.get(id);
			gui.update(this, handler.getInput(), width, height, delta);
			for (GUIAction action : gui.fetchActions()) {
				if (action.executeOnClient()) {
					action.perform(level, handler, id, this, camera);
				}
				else {
					actions.add(new Pair<Long, GUIAction>(id, action));
				}
			}
		}
		return actions;
	}

	@Override
	public Map<Long, GUI> getGUIs() {
		return guis.asMap();
	}

	@Override
	public void showGUI(String type) {
		GUI gui = factory.createGUI(type);
		long id = guis.add(gui);
		listeners.forEach(l -> l.onAddGUI(this, gui, id));
	}

	@Override
	public void showGUI(String type, long id) {
		GUI gui = factory.createGUI(type);
		guis.add(gui, id);
		listeners.forEach(l -> l.onAddGUI(this, gui, id));
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public GUI getGUI(long id) {
		return guis.get(id);
	}

}