package abdom.location.filter;

import java.util.List;

import abdom.location.Plot;

public interface PlotsFilter {
	List<Plot> apply(List<Plot> plots);
}
