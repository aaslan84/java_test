import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.*;

class SelectingEditor extends DefaultCellEditor {
    public SelectingEditor(JTextField textField) {
      super(textField);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
        int column) {
      Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
      if (c instanceof JTextComponent) {
        JTextComponent jtc = (JTextComponent) c;
        jtc.requestFocus();
        jtc.selectAll();
      }
      return c;
    }
  }