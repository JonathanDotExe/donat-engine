package at.jojokobi.donatengine.ressources;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

public interface IRessourceHandler {
	
	public Image getImage (String key);
	
	public Media getAudio (String key);
	
	public <T> T getRessource (String key, Class<T> clazz);
	
	public boolean putImage (String key, String path);
	
	public boolean putAudio (String key, String path);
	
	public boolean putRessource (String key, String path, RessourceLoader loader);

}