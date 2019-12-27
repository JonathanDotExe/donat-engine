package at.jojokobi.donatengine.gui.style;

import at.jojokobi.donatengine.gui.nodes.Node;

public class FitWidthDimension implements Dimension{

	@Override
	public Double getValue(double parent, Node node) {
		return node.getFitBounds().getWidth();
	}
	
}