/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package recorder.gui;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import recorder.Recorder;
import recorder.RecorderListener;
import recorder.component.GuiComponentFactory;
import recorder.gui.action.AbstractGuiAction;
import recorder.gui.action.GuiAction;
import recorder.gui.action.GuiActionManager;
import recorder.gui.assertion.AssertManager;
import recorder.gui.assertion.DialogManager;
import recorder.gui.icons.IconsManager;
import recorder.gui.script.ScriptLogic;
import recorder.gui.util.ComponentHighlighter;
import recorder.gui.util.HighlightListener;
/**
 */
public class RecorderLogic {
    private static final Logger LOG = Logger.getLogger(RecorderLogic.class);
    private RecorderGui gui;
    private GuiActionManager actionManager = new GuiActionManager();
    private AssertManager assertManager;
    private Recorder recorder;
    private GuiComponentFactory factory = new GuiComponentFactory();
    private RecorderListener listener =
        new RecorderListener() {
            public void recorderUpdate() {
                gui.display(recorder.getGestureResultList());
            }
        };
    private static final String RECORD_START = "record.start";
    private static final String RECORD_STOP = "record.stop";
    private static final String LOG_CLEAR = "log.clear";
    private static final String SCRIPT_CLEAR = "script.clear";
    private static final String SCRIPT_CLEAR_LAST = "script.clear.last";
    private static final String HIGHLIGHT = "highlight";

    public RecorderLogic() {
        recorder = new Recorder(factory);
        assertManager = new AssertManager(recorder, new DialogManager());

        StartAction startAction = new StartAction();
        StopAction stopAction = new StopAction();

        startAction.setEnabled(true);
        stopAction.setEnabled(false);

        actionManager.declare(startAction);
        actionManager.declare(stopAction);
        actionManager.declare(new ClearScriptAction());
        actionManager.declare(new HighLightAction(factory));
        actionManager.declare(new ClearLogAction());
        actionManager.declare(new RemoveLastGestureAction());

        gui = new RecorderGui();
        gui.setName("recorder.mainPanel");
        factory.setIgnoredContainer("recorder.mainPanel");

        gui.setStartAction(actionManager.getAction(RECORD_START));
        gui.setStopAction(actionManager.getAction(RECORD_STOP));
        gui.setClearLogAction(actionManager.getAction(LOG_CLEAR));
        gui.setClearAction(actionManager.getAction(SCRIPT_CLEAR));
        gui.setRemoveLastAction(actionManager.getAction(SCRIPT_CLEAR_LAST));
        gui.setHighlightAction(actionManager.getAction(HIGHLIGHT));

        new ScriptLogic(gui.getScriptGui(), recorder);

        recorder.addRecorderListener(listener);

        Logger.getLogger("recorder").addAppender(new LogAreaAppender(gui));
        LOG.info("Recorder en route...");
        assertManager.start();
    }

    public JPanel getGui() {
        return gui;
    }


    public void display() {
        JFrame frame = new JFrame("EveRe - 1.00-SNAPSHOT");

//        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        frame.setContentPane(getGui());
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    System.exit(0);
                }
            });
        frame.pack();
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.show();
    }


    public void startRecord() {
        actionManager.getAction("record.start").execute();
    }


    public void stopRecord() {
        actionManager.getAction("record.stop").execute();
    }


    public void clearRecord() {
        actionManager.getAction("script.clear").execute();
    }


    public void clearLog() {
        actionManager.getAction("log.clear").execute();
    }

    private class StartAction extends AbstractGuiAction {
        StartAction() {
            super("record.start");
            putValue(GuiAction.ICON_ID, IconsManager.RECORD_START);
            putValue(GuiAction.TOOLTIP, "Enregistre les actions de l'utilisateur");
        }

        public void execute() {
            this.setEnabled(false);
            actionManager.getAction("record.stop").setEnabled(true);
            recorder.startRecord();
        }
    }


    private class StopAction extends AbstractGuiAction {
        StopAction() {
            super("record.stop");
            putValue(GuiAction.ICON_ID, IconsManager.RECORD_STOP);
            putValue(GuiAction.TOOLTIP, "Stop l'enregistrement des actions utilisateur");
        }

        public void execute() {
            this.setEnabled(false);
            actionManager.getAction("record.start").setEnabled(true);
            recorder.stopRecord();
        }
    }


    private class ClearScriptAction extends AbstractGuiAction {
        ClearScriptAction() {
            super("script.clear");
            putValue(GuiAction.ICON_ID, IconsManager.CLEAR);
            putValue(GuiAction.TOOLTIP, "Efface le script courant");
        }

        public void execute() {
            recorder.clearScript();
        }
    }


    private class HighLightAction extends AbstractGuiAction implements HighlightListener {
        private ComponentHighlighter componentHighlighter;
        private boolean highlight = false;

        HighLightAction(GuiComponentFactory factory) {
            super("highlight");
            componentHighlighter = new ComponentHighlighter(factory);
            componentHighlighter.setListener(this);
            updateIconAndTooltip();
        }

        public void execute() {
            if (!highlight) {
                componentHighlighter.start();
            }
            else {
                componentHighlighter.stop();
            }
            highlight = !highlight;
            updateIconAndTooltip();
        }


        private void updateIconAndTooltip() {
            if (highlight) {
                putValue(GuiAction.ICON_ID, IconsManager.HIGHLIGHT_STOP);
                putValue(GuiAction.TOOLTIP,
                    "Stop la mise en surbrillance des composants GUI testable");
            }
            else {
                putValue(GuiAction.ICON_ID, IconsManager.HIGHLIGHT_START);
                putValue(GuiAction.TOOLTIP,
                    "Mise en surbrillance des composants GUI testable");
            }
        }


        public void highlight(JComponent component) {
            gui.displayHierarchy(component);
        }
    }


    private class ClearLogAction extends AbstractGuiAction {
        ClearLogAction() {
            super("log.clear");
            putValue(GuiAction.ICON_ID, IconsManager.CLEAR);
            putValue(GuiAction.TOOLTIP, "Efface les log");
        }

        public void execute() {
            gui.clearLogArea();
        }
    }


    private class RemoveLastGestureAction extends AbstractGuiAction
        implements RecorderListener {
        RemoveLastGestureAction() {
            super("script.clear.last");
            putValue(GuiAction.ICON_ID, IconsManager.REMOVE_LAST);
            putValue(GuiAction.TOOLTIP, "Supprime le dernier �l�ment du script");
            setEnabled(false);
            recorder.addRecorderListener(this);
        }

        public void execute() {
            recorder.removeLastGesture();
        }


        public void recorderUpdate() {
            boolean hasGesture = (recorder.getGestureResultList().size() > 0);
            setEnabled(hasGesture);
        }
    }


    private static class LogAreaAppender extends AppenderSkeleton {
        private RecorderGui logGui;

        LogAreaAppender(RecorderGui logGui) {
            this.logGui = logGui;
        }

        protected void append(LoggingEvent event) {
            StringBuffer buffer = new StringBuffer();
            if (event.getLevel() == Level.DEBUG) {
                buffer.append("> ");
            }
            buffer.append(event.getMessage().toString());
            logGui.displayLog(buffer.toString());
        }


        public void close() {}


        public boolean requiresLayout() {
            return false;
        }
    }
}