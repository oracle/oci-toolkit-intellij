package com.oracle.oci.intellij.ui.appstack;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.ui.DialogWrapper;
import com.oracle.bmc.resourcemanager.model.JobSummary;
import com.oracle.oci.intellij.account.OracleCloudAccount;
import com.oracle.oci.intellij.ui.appstack.command.TerraformLogger;

class StackJobDialog extends DialogWrapper {

  private final List<JobSummary> jobs;      
  private TerraformLogger logger;


  protected StackJobDialog(List<JobSummary> jobs) {
    super(true);
    this.jobs = new ArrayList<>(jobs);
    init();
    setTitle("Stack Job");
    setOKButtonText("Ok");
  }

  @Override
  protected void dispose() {
    super.dispose();
    if (logger != null) {
      logger.close();
      logger = null;
    }
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));

    DefaultTableModel jobsModel = new DefaultTableModel();
    jobsModel.addColumn("Name");
    jobsModel.addColumn("Operation");
    jobsModel.addColumn("Status");
    jobsModel.addColumn("Time Created");
    List<Object> row = new ArrayList<>();
    this.jobs.forEach(j -> {
      row.add(j.getDisplayName());
      row.add(j.getOperation());
      row.add(j.getLifecycleState());
      row.add(j.getTimeCreated());
      jobsModel.addRow(row.toArray());
      row.clear();
    });

    JTable jobsTable = new JTable();
    jobsTable.setModel(jobsModel);
    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.add(jobsTable, BorderLayout.NORTH);
    centerPanel.add(leftPanel);

    JTextArea textArea = new JTextArea();
    // textArea.setText("Hello!");
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setVisible(true);
    textArea.setColumns(80);
    textArea.setRows(30);

    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    centerPanel.add(scroll);
    // centerPanel.add(textArea);

    jobsTable.addMouseListener(new MouseAdapter() {


      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (jobsTable.getSelectedRowCount() == 1) {
            int selectedRow = jobsTable.getSelectedRow();
            JobSummary jobSummary = jobs.get(selectedRow);
            String id = jobSummary.getId();
            textArea.setText(null);
            if (logger != null) {
              logger.close();
            }
            logger =
              new TerraformLogger(new JTextAreaWriter(textArea),
                                  OracleCloudAccount.getInstance()
                                                    .getResourceManagerClientProxy(),
                                  id);
            logger.start();
          }
        }
      }
    });

    return centerPanel;
  }

  private static class JTextAreaWriter extends Writer {
    private JTextArea target;
    private StringBuilder buffer;

    public JTextAreaWriter(JTextArea target) {
      super();
      this.target = target;
      this.buffer = new StringBuilder();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      this.buffer.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
      if (this.buffer.length() > 0) {
        try {
          // wait to ensure ordering.
          SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
              if (buffer.length() > 0) {
                StringBuilder fullBuffer = new StringBuilder();
                fullBuffer.append(target.getText());
                fullBuffer.append(buffer);
                target.setText(fullBuffer.toString());
                buffer.setLength(0);
              }
            }
          });
        } catch (InvocationTargetException | InterruptedException e) {
          throw new IOException(e);
        }
      }
    }

    @Override
    public void close() throws IOException {
      this.buffer = null;
      this.target = null;
    }
  }
}