/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package recorder.gui.assertion;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import recorder.gui.action.GuiAction;
import recorder.result.AttributeList;
import recorder.result.DefaultStatement;
/**
 * Permet de cr�er un tag assertValue.
 */
class AssertValue extends AbstractAssert {
    static final String ID = "assertion.value";
    private AssertContext context;

    AssertValue(AssertContext assertContext) {
        super(ID, COMPONENT_ASSERT);
        putValue(GuiAction.LABEL, "AssertValue");
        putValue(GuiAction.TOOLTIP,
            "Ajoute un 'assertion' sur la valeur d'un JTextComponent");
        this.context = assertContext;
    }

    public void execute() {
        Object component = context.getSource();
        if (!(component instanceof JTextComponent
                || component instanceof JComboBox
                || component instanceof JCheckBox)) {
            return;
        }

        AttributeList attributes = new AttributeList();
        attributes.put("name", ((JComponent)component).getName());

        if (component instanceof JTextComponent) {
            attributes.put("expected", ((JTextComponent)component).getText());
        }
        else if (component instanceof JCheckBox) {
            attributes.put("expected",
                (((JCheckBox)component).isSelected() ? "true" : "false"));
        }
        else {
            attributes.put("expected", ((JComboBox)component).getSelectedItem());
        }

        context.postAssert(new DefaultStatement("assertValue", attributes));
    }


    public void update() {
        Object component = context.getSource();
        this.setEnabled((component instanceof JTextComponent
            || component instanceof JComboBox || component instanceof JCheckBox)
            && context.isFindableComponent());
    }
}