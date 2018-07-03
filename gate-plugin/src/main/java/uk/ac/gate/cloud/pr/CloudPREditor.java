package uk.ac.gate.cloud.pr;

import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Resource;
import gate.VisualResource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.gui.SerialControllerEditor;
import gate.util.GateException;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

import uk.ac.gate.cloud.online.ServiceMetadata;

@CreoleResource(
        name = "Service Configuration",
        comment = "Configures annotation selectors and annotation set mappings",
        guiType = GuiType.LARGE,
        resourceDisplayed = "uk.ac.gate.cloud.pr.GateCloudPR",
        mainViewer = true)
public class CloudPREditor extends AbstractVisualResource {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private GateCloudPR pr;

  private JButton fetchMetadataButton;

  private JPanel selectorsPanel;

  private JCheckBox[] selectorBoxes;

  private MappingsTableModel mappingsTableModel;
  
  private JTable mappingsTable;

  private ServiceMetadata serviceMetadata;

  public Resource init() throws ResourceInstantiationException {
    fetchMetadataButton = new JButton("Fetch service metadata...");
    fetchMetadataButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        refreshMetadata();
      }
    });

    Box buttonBox = Box.createHorizontalBox();
    buttonBox.add(Box.createHorizontalGlue());
    buttonBox.add(fetchMetadataButton);
    buttonBox.add(Box.createHorizontalGlue());
    selectorsPanel = new JPanel();
    selectorsPanel.setLayout(new BorderLayout());
    JScrollPane selectorsScroller = new JScrollPane(selectorsPanel);
    selectorsScroller.setBorder(BorderFactory
            .createTitledBorder("Annotation types"));
    mappingsTableModel = new MappingsTableModel();
    mappingsTable = new JTable(mappingsTableModel);
    mappingsTable.setDefaultRenderer(String.class, cellRenderer);
    JScrollPane mappingsScroller = new JScrollPane(mappingsTable);
    mappingsScroller.setBorder(BorderFactory
            .createTitledBorder("Annotation sets"));
    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    // hack to make sure the divider starts in the middle
    splitPane.addComponentListener(new ComponentAdapter() {
      private boolean firstResize = true;

      @Override
      public void componentResized(ComponentEvent e) {
        if(firstResize) {
          splitPane.setDividerLocation(0.5);
          firstResize = false;
        }
      }
      
    });
    splitPane.setTopComponent(selectorsScroller);
    splitPane.setBottomComponent(mappingsScroller);
    splitPane.setResizeWeight(0.5);

    setLayout(new BorderLayout());
    add(buttonBox, BorderLayout.NORTH);
    add(splitPane, BorderLayout.CENTER);
    
    addAncestorListener(new AncestorListener() {
      
      @Override
      public void ancestorRemoved(AncestorEvent event) {
      }
      
      @Override
      public void ancestorMoved(AncestorEvent event) {
      }
      
      @Override
      public void ancestorAdded(AncestorEvent event) {
        // fetch parameter values from the PR when switching to this VR
        refreshState();
      }
    });

    return this;
  }

  public void setTarget(Object target) {
    pr = (GateCloudPR)target;
  }

  protected void refreshMetadata() {
    fetchMetadataButton.setEnabled(false);
    new SwingWorker<Void, Void>() {

      @Override
      protected Void doInBackground() throws Exception {
        serviceMetadata = pr.getEndpoint().metadata();
        return null;
      }

      public void done() {
        metadataUpdated();
        fetchMetadataButton.setEnabled(true);
      }

    }.execute();
  }

  private void metadataUpdated() {
    SerialControllerEditor.clearAllSelections();
    // clear out existing components
    selectorsPanel.removeAll();
    
    SortedSet<String> asNames = new TreeSet<>();

    String[] defaultSelectors =
            (serviceMetadata.defaultAnnotations == null || ""
                    .equals(serviceMetadata.defaultAnnotations))
                    ? new String[0]
                    : serviceMetadata.defaultAnnotations.split("\\s*,\\s*");
    String[] additionalSelectors =
            (serviceMetadata.additionalAnnotations == null || ""
                    .equals(serviceMetadata.additionalAnnotations))
                    ? new String[0]
                    : serviceMetadata.additionalAnnotations.split("\\s*,\\s*");

    // one array of all selectors
    String[] allSelectors =
            new String[defaultSelectors.length + additionalSelectors.length];
    System.arraycopy(defaultSelectors, 0, allSelectors, 0,
            defaultSelectors.length);
    System.arraycopy(additionalSelectors, 0, allSelectors,
            defaultSelectors.length, additionalSelectors.length);

    Map<String, Integer> numSets = new HashMap<String, Integer>();
    for(String sel : allSelectors) {
      String[] splitSel = sel.split(":");
      asNames.add(splitSel[0]);
      int count =
              numSets.containsKey(splitSel[1]) ? numSets.get(splitSel[1]) : 0;
      count++;
      numSets.put(splitSel[1], count);
    }
    selectorBoxes = new JCheckBox[allSelectors.length];
    Box selectorsBox = Box.createVerticalBox();
    for(int i = 0; i < allSelectors.length; i++) {
      String[] splitSel = allSelectors[i].split(":");
      String buttonText = splitSel[1];
      if(numSets.get(splitSel[1]) > 1) {
        if(splitSel[0].equals("")) {
          buttonText += " (default annotation set)";
        } else {
          buttonText += " (annotation set \"" + splitSel[1] + "\")";
        }
      }
      selectorBoxes[i] = new JCheckBox(buttonText);
      selectorBoxes[i].addChangeListener(selectorChangeListener);
      selectorBoxes[i].setActionCommand(allSelectors[i]);
      selectorsBox.add(selectorBoxes[i]);
    }
    selectorsPanel.add(selectorsBox, BorderLayout.CENTER);
    
    // annotation set mappings
    List<String> allAsNames = new ArrayList<>(asNames);
    FeatureMap mappings = pr.getAnnotationSetMapping();
    if(mappings == null) {
      mappings = Factory.newFeatureMap();
      pr.setAnnotationSetMapping(mappings);
    }
    for(String name : allAsNames) {
      if(!mappings.containsKey(name)) {
        // by default, map everything to itself
        mappings.put(name, name);
      }
    }
    mappingsTableModel.setAsNames(allAsNames);
    
    revalidate();
    
    refreshState();
  }
  
  /**
   * Pull the current parameter values from the PR and update our views.
   */
  protected void refreshState() {
    if(serviceMetadata == null) return;
    
    String selectorsFromPr = pr.getAnnotationSelectors();
    if(selectorsFromPr == null || selectorsFromPr.trim().equals("")) {
      selectorsFromPr = serviceMetadata.defaultAnnotations;
    }
    Set<String> selectors = new HashSet<>(Arrays.asList(selectorsFromPr.trim().split("\\s*,\\s*")));
    for(JCheckBox box : selectorBoxes) {
      box.setSelected(selectors.contains(box.getActionCommand()));
    }
    
    mappingsTableModel.fireTableDataChanged();
  }

  private ChangeListener selectorChangeListener = new ChangeListener() {

    @Override
    public void stateChanged(ChangeEvent e) {
      StringBuilder buf = new StringBuilder();
      boolean needComma = false;
      for(JCheckBox box : selectorBoxes) {
        if(box.isSelected()) {
          if(needComma) buf.append(", ");
          needComma = true;
          buf.append(box.getActionCommand()); // the selector
        }
      }
      String selectors = buf.toString();
      if("".equals(selectors)) {
        selectors = null;
      }
      SerialControllerEditor.clearAllSelections();
      pr.setAnnotationSelectors(selectors);
    }
  };
  
  private DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
      boolean defaultAS = (value == null || value.equals(""));
      if(defaultAS) {
        value = "<default annotation set>";
      }

      Component renderer = super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column);
            
      if(defaultAS) {
        renderer.setFont(renderer.getFont().deriveFont(Font.ITALIC));
      }
      
      return renderer;
    }
    
  };
  
  private class MappingsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    
    private List<String> asNames;
    
    public void setAsNames(List<String> asNames) {
      this.asNames = asNames;
    }

    @Override
    public int getRowCount() {
      return (asNames == null ? 0 : asNames.size());
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      String asName = asNames.get(rowIndex);
      if(columnIndex == 0) {
        return asName;
      } else {
        return pr.getAnnotationSetMapping().get(asName);
      }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      String asName = asNames.get(rowIndex);
      pr.getAnnotationSetMapping().put(asName, aValue);
      fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
      if(column == 0) {
        return "Map this set from the service...";
      } else {
        return "...to this set in the document";
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return (columnIndex == 1);
    }
  }
}
