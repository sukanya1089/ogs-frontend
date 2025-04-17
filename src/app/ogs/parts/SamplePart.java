package app.ogs.parts;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import app.ogs.BackendClient;
import app.ogs.model.Sample;
import app.ogs.model.Statistics;
import app.ogs.model.UnitSystem;
import app.ogs.views.AddSampleDialog;
import app.ogs.views.EditSampleDialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
public class SamplePart {


	private UnitSystem currentUnitSystem = UnitSystem.METRIC;

	private TableViewer tableViewer;
    private ComboViewer locationFilterCombo;
    private Label averageWaterLabel;
    private Label thresholdCountLabel;

    private Composite chartComposite;
    private ComboViewer parameterSelector;
    private ChartPanel chartPanel;

    private List<Sample> allSamples = new ArrayList<>();


	@Inject
	private MPart part;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		
        createLocationFilter(parent);
        createTableViewer(parent);
        createButtons(parent);
        createStatisticsArea(parent);
		createUnitSwitch(parent);
		createGraphArea(parent);

        locationFilterCombo.addSelectionChangedListener(event -> filterByLocation());

        fetchAndLoadSamples();
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		part.setDirty(false);
	}

	private void createUnitSwitch(Composite parent) {
	    Group unitGroup = new Group(parent, SWT.NONE);
	    unitGroup.setText("Unit System");
	    unitGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
	    unitGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

	    Button metricButton = new Button(unitGroup, SWT.RADIO);
	    metricButton.setText("Metric (SI)");
	    metricButton.setSelection(true);

	    Button usButton = new Button(unitGroup, SWT.RADIO);
	    usButton.setText("US Customary");

	    Listener switchListener = e -> {
	        currentUnitSystem = metricButton.getSelection() ? UnitSystem.METRIC : UnitSystem.US;
	        updateDisplayForUnitSystem();
	    };

	    metricButton.addListener(SWT.Selection, switchListener);
	    usButton.addListener(SWT.Selection, switchListener);
	}
	
	private void updateDisplayForUnitSystem() {
	    IStructuredSelection selection = locationFilterCombo.getStructuredSelection();
	    String selectedLocation = (String) selection.getFirstElement();

	    List<Sample> filtered = allSamples.stream()
	        .filter(s -> selectedLocation.equals("All") || s.getLocation().equals(selectedLocation))
	        .toList();

	    tableViewer.setInput(filtered);
	    updateFilteredStatistics(filtered);
	}



    private void createLocationFilter(Composite parent) {
        new Label(parent, SWT.NONE).setText("Filter by Location:");
        locationFilterCombo = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        locationFilterCombo.setContentProvider(ArrayContentProvider.getInstance());
        // locationFilterCombo.addSelectionChangedListener(event -> filterByLocation());

        // Fetch locations from back-end
        List<String> locations = new ArrayList<>(BackendClient.getLocations());
        locations.add(0, "All");
        locationFilterCombo.setInput(locations);
        locationFilterCombo.setSelection(new StructuredSelection("All"));
    }

    private void createTableViewer(Composite parent) {
    	tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
    	tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        createColumns(tableViewer);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        table.setLayoutData(gridData);
    }


    private void createColumns(TableViewer viewer) {
        String[] titles = { "ID", "Location", "Depth", "Date", "Unit Weight", "Water Content", "Shear Strength" };
        int[] bounds = { 100, 100, 80, 120, 120, 120, 120 };

        for (int i = 0; i < titles.length; i++) {
            final int index = i;
            TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
            col.getColumn().setText(titles[i]);
            col.getColumn().setWidth(bounds[i]);
            col.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    Sample s = (Sample) element;
                    return switch (index) {
                        case 0 -> s.getId();
                        case 1 -> s.getLocation();
                        case 2 -> formatDepth(s.getDepth());
                        case 3 -> s.getDateCollected().toString();
                        case 4 -> formatUnitWeight(s.getUnitWeight());
                        case 5 -> s.getWaterContent() + " %";
                        case 6 -> formatShearStrength(s.getShearStrength());
                        default -> "";
                    };
                }
            });
        }
    }
    
    private String formatDepth(double meters) {
        return currentUnitSystem == UnitSystem.METRIC
            ? String.format("%.2f m", meters)
            : String.format("%.2f ft", meters * 3.28084);
    }

    private String formatUnitWeight(double knm3) {
        return currentUnitSystem == UnitSystem.METRIC
            ? String.format("%.2f kN/m³", knm3)
            : String.format("%.2f lb/ft³", knm3 * 6.36588);
    }

    private String formatShearStrength(double kpa) {
        return currentUnitSystem == UnitSystem.METRIC
            ? String.format("%.2f kPa", kpa)
            : String.format("%.2f psi", kpa * 0.145038);
    }



    private void createButtons(Composite parent) {
        Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        buttonBar.setLayoutData(gridData);

        Button addBtn = new Button(buttonBar, SWT.PUSH);
        addBtn.setText("Add Sample");
        addBtn.addListener(SWT.Selection, e -> {
            AddSampleDialog dialog = new AddSampleDialog(parent.getShell());
            if (dialog.open() == Window.OK) fetchAndLoadSamples();
        });

        Button editBtn = new Button(buttonBar, SWT.PUSH);
        editBtn.setText("Edit Selected");
        editBtn.addListener(SWT.Selection, e -> editSelectedSample(parent));

        Button deleteBtn = new Button(buttonBar, SWT.PUSH);
        deleteBtn.setText("Delete Selected");
        deleteBtn.addListener(SWT.Selection, e -> deleteSelectedSample(parent));

        Button refreshBtn = new Button(buttonBar, SWT.PUSH);
        refreshBtn.setText("Refresh");
        refreshBtn.addListener(SWT.Selection, e -> fetchAndLoadSamples());
    }
    

    private void editSelectedSample(Composite parent) {
        IStructuredSelection selection = tableViewer.getStructuredSelection();
        if (selection.isEmpty()) {
            MessageDialog.openInformation(parent.getShell(), "No selection", "Please select a sample to edit.");
            return;
        }

        Sample selected = (Sample) selection.getFirstElement();
        EditSampleDialog dialog = new EditSampleDialog(parent.getShell(), selected);
        if (dialog.open() == Window.OK) {
            fetchAndLoadSamples();
        }
    }

    private void deleteSelectedSample(Composite parent) {
        IStructuredSelection selection = tableViewer.getStructuredSelection();
        if (selection.isEmpty()) {
            MessageDialog.openInformation(parent.getShell(), "No selection", "Please select a sample to delete.");
            return;
        }

        Sample selected = (Sample) selection.getFirstElement();
        boolean confirm = MessageDialog.openConfirm(parent.getShell(), "Confirm Delete",
                "Are you sure you want to delete this sample?");

        if (confirm) {
            BackendClient.deleteSample(selected.getId());
            fetchAndLoadSamples();
        }
    }


    private void createStatisticsArea(Composite parent) {
        Group statsGroup = new Group(parent, SWT.NONE);
        statsGroup.setText("Statistics");
        statsGroup.setLayout(new GridLayout(1, false));
        statsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        averageWaterLabel = new Label(statsGroup, SWT.NONE);
        thresholdCountLabel = new Label(statsGroup, SWT.NONE);
    }


    private void fetchAndLoadSamples() {
    	// TODO: Move filtering to back-end to reduce stress on UI and faster responses
        allSamples = BackendClient.getAllSamples();
        tableViewer.setInput(allSamples);
        updateStatistics(allSamples);
    }

    private void filterByLocation() {
        String selectedLocation = (String) ((IStructuredSelection) locationFilterCombo.getSelection()).getFirstElement();
        List<Sample> filtered;
        if ("All".equals(selectedLocation)) {
        	filtered = allSamples;
        } else {
        	filtered = allSamples.stream().filter(s -> s.getLocation().equals(selectedLocation)).toList();
        }
        tableViewer.setInput(filtered);
        updateFilteredStatistics(filtered);
        updateChart(filtered);
    }

    private void updateStatistics(List<Sample> currentSamples) {
        // Show overall stats regardless of filter
        Statistics stats = BackendClient.getStatistics();

        averageWaterLabel.setText("Average Water Content: " + String.format("%.2f %%", stats.getAverageWaterContent()));

        String thresholdText = String.format(
                "Samples exceeding thresholds:\n- Unit Weight: %d\n- Water Content: %d\n- Shear Strength: %d",
                stats.getSamplesAboveUnitWeightThreshold(),
                stats.getSamplesAboveWaterContentThreshold(),
                stats.getSamplesAboveShearStrengthThreshold()
        );

        thresholdCountLabel.setText(thresholdText);
        averageWaterLabel.getParent().layout(); // Refresh the layout
    }

    private void updateFilteredStatistics(List<Sample> samples) {
        double avgWater = samples.stream()
                .mapToDouble(Sample::getWaterContent)
                .average()
                .orElse(0.0);

        averageWaterLabel.setText("Average Water Content: " + String.format("%.2f %%", avgWater));
        thresholdCountLabel.setText("(Threshold stats only reflect all samples)");
        averageWaterLabel.getParent().layout();
    }
    
    private void createGraphArea(Composite parent) {
        Group graphGroup = new Group(parent, SWT.NONE);
        graphGroup.setText("Depth vs Parameter");
        graphGroup.setLayout(new GridLayout(2, false));
        graphGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        new Label(graphGroup, SWT.NONE).setText("Parameter:");

        parameterSelector = new ComboViewer(graphGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        parameterSelector.setContentProvider(ArrayContentProvider.getInstance());
        parameterSelector.setInput(List.of("Unit Weight", "Water Content", "Shear Strength"));
        parameterSelector.setSelection(new StructuredSelection("Unit Weight"));

        chartComposite = new Composite(graphGroup, SWT.EMBEDDED);
        GridData chartGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        chartComposite.setLayoutData(chartGrid);

        parameterSelector.addSelectionChangedListener(e -> updateChart());
        createChart(List.of()); // empty initial chart
    }
    
    private void createChart(List<Sample> samples) {
        String selected = (String) ((IStructuredSelection) parameterSelector.getSelection()).getFirstElement();

        XYSeries series = new XYSeries(selected);
        for (Sample s : samples) {
            double yValue = switch (selected) {
                case "Water Content" -> s.getWaterContent();
                case "Shear Strength" -> s.getShearStrength();
                default -> s.getUnitWeight(); // "Unit Weight"
            };
            series.add(s.getDepth(), yValue);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
            selected + " vs Depth",
            "Depth (m)",
            selected,
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false
        );

        // Embed chart in SWT
        if (chartComposite != null && !chartComposite.isDisposed()) {
            Frame frame = SWT_AWT.new_Frame(chartComposite);
            chartPanel = new ChartPanel(chart);
            frame.add(chartPanel);
            chartComposite.layout();
        }
    }

    private void updateChart() {
        IStructuredSelection selection = locationFilterCombo.getStructuredSelection();
        String selectedLocation = (String) selection.getFirstElement();

        List<Sample> filtered = allSamples.stream()
            .filter(s -> selectedLocation.equals("All") || s.getLocation().equals(selectedLocation))
            .toList();

        updateChart(filtered);
    }

    private void updateChart(List<Sample> filteredSamples) {
        if (chartPanel != null) chartPanel.setChart(null); // Clear before redraw
        createChart(filteredSamples);
    }



}