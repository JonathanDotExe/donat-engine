package at.jojokobi.donatengine;

import java.util.List;

import at.jojokobi.donatengine.rendering.RenderData;

public interface GameLogic {
	
	public void start (Game game);

	public void update (double delta, Game game);
	
	public void render (List<RenderData> data);
	
	public void stop (Game game);
	
}
