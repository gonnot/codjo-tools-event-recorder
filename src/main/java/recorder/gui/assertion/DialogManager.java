/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package recorder.gui.assertion;
import javax.swing.JPopupMenu;
/**
 * Manager des dialogues (interactions) avec l'utilisateur.
 */
public class DialogManager {
    public JPopupMenu newPopupMenu() {
        return new JPopupMenu();
    }
}