package de.jamesbeans.quadrasolve;

import co.ceryle.segmentedbutton.SegmentedButtonGroup;

import static de.jamesbeans.quadrasolve.GraphActivity.*;

/**
 * Manages the segmented button control in the graph view.
 *
 * Created by Simon on 04.01.2018.
 */

class ModeButtonClickListener implements SegmentedButtonGroup.OnClickedButtonListener {
    private static final int TRACING_POSITION = 0;
    private static final int PANNING_POSITION = 1;
    private final GraphView g;

    ModeButtonClickListener(GraphView g) {
        this.g = g;
    }

    @Override
    public void onClickedButton(int position) {
        switch(position) {
            case TRACING_POSITION:
                g.activity = TRACING;
                break;
            case PANNING_POSITION:
                g.activity = PANNING;
                break;
        }
    }
}
