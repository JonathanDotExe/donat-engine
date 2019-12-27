package at.jojokobi.donatengine.leveleditor;

import java.io.InputStream;
import java.io.OutputStream;

import at.jojokobi.donatengine.level.Level;

public interface MapLoader {

	public void load (InputStream stream, Level level)  throws InvalidLevelFileException;
	
	public void save (OutputStream stream, Level level);
	
}