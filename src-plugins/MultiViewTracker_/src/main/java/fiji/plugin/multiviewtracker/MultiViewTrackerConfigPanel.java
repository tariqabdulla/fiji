package fiji.plugin.multiviewtracker;

import static fiji.plugin.trackmate.gui.TrackMateWizard.BIG_FONT;
import static fiji.plugin.trackmate.gui.TrackMateWizard.FONT;
import static fiji.plugin.trackmate.gui.TrackMateWizard.SMALL_FONT;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_DISPLAY_SPOT_NAMES;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_SPOTS_VISIBLE;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_SPOT_COLOR_FEATURE;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_SPOT_RADIUS_RATIO;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACKS_VISIBLE;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_DISPLAY_DEPTH;
import static fiji.plugin.trackmate.visualization.TrackMateModelView.KEY_TRACK_DISPLAY_MODE;
import static fiji.plugin.trackmate.visualization.trackscheme.TrackSchemeFrame.TRACK_SCHEME_ICON;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import fiji.plugin.trackmate.TrackMateModel;
import fiji.plugin.trackmate.gui.ActionListenablePanel;
import fiji.plugin.trackmate.gui.JNumericTextField;
import fiji.plugin.trackmate.gui.JPanelColorByFeatureGUI;
import fiji.plugin.trackmate.visualization.TrackMateModelView;

public class MultiViewTrackerConfigPanel <T extends RealType<T> & NativeType<T>> extends JFrame {

	
	private static final long serialVersionUID = 1L;

	public ActionEvent TRACK_SCHEME_BUTTON_PRESSED 	= new ActionEvent(this, 0, "TrackSchemeButtonPushed");

	private JButton jButtonShowTrackScheme;
	private JLabel jLabelTrackDisplayMode;
	private JComboBox jComboBoxDisplayMode;
	private JLabel jLabelDisplayOptions;
	private JPanel jPanelSpotOptions;
	private JCheckBox jCheckBoxDisplaySpots;
	private JPanel jPanelTrackOptions;
	private JCheckBox jCheckBoxDisplayTracks;
	private JCheckBox jCheckBoxLimitDepth;
	private JTextField jTextFieldFrameDepth;
	private JLabel jLabelFrameDepth;
	private JPanelColorByFeatureGUI jPanelSpotColor;
	private JNumericTextField jTextFieldSpotRadius;
	private JCheckBox jCheckBoxDisplayNames;
	/** The set of {@link TrackMateModelView} views controlled by this controller.	 */
	private Set<TrackMateModelView<T>> views = new HashSet<TrackMateModelView<T>>();

	private ActionListenablePanel mainPanel;

	private TrackMateModel<T> model;

	/*
	 * CONSTRUCTOR
	 */
	public MultiViewTrackerConfigPanel(TrackMateModel<T> model, MultiViewDisplayer<T> view) {
		this.model = model;
		register(view);
		initGUI();
	}

	/*
	 * METHODS
	 */
	
	protected void fireAction(final ActionEvent event) {
		new Thread() {
			@Override
			public void run() {
				// Intercept event coming from the JPanelSpotColorGUI, and translate it for views
				if (event == jPanelSpotColor.COLOR_FEATURE_CHANGED) {
					for (TrackMateModelView<T> view : views) {
						view.setDisplaySettings(KEY_SPOT_COLOR_FEATURE, jPanelSpotColor.getSelectedFeature());
						view.refresh();
					}
				} else {
					System.out.println("Got event: "+event);
				}
			}
		}.start();
	}
	
	/**
	 * Add the given {@link TrackMateModelView} to the list managed by this controller.
	 */
	public void register(final TrackMateModelView<T> view) {
		if (!views.contains(view)) {
			views.add(view);
		}
	}
	
	/*
	 * PRIVATE METHODS
	 */
	
	private void initGUI() {
		try {
			
			mainPanel = new ActionListenablePanel();
			mainPanel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					fireAction(event);
				}
			});
			mainPanel.setSize(300, 500);
			mainPanel.setLayout(null);
			{
				jPanelTrackOptions = new JPanel() {
					private static final long serialVersionUID = -1805693239189343720L;
					public void setEnabled(boolean enabled) {
						for(Component c : getComponents())
							c.setEnabled(enabled);
					};
				};
				FlowLayout jPanelTrackOptionsLayout = new FlowLayout();
				jPanelTrackOptionsLayout.setAlignment(FlowLayout.LEFT);
				jPanelTrackOptions.setLayout(jPanelTrackOptionsLayout);
				mainPanel.add(jPanelTrackOptions);
				jPanelTrackOptions.setBounds(10, 212, 280, 117);
				jPanelTrackOptions.setBorder(new LineBorder(new java.awt.Color(192,192,192), 1, true));
				{
					jLabelTrackDisplayMode = new JLabel();
					jPanelTrackOptions.add(jLabelTrackDisplayMode);
					jLabelTrackDisplayMode.setText("  Track display mode:");
					jLabelTrackDisplayMode.setBounds(10, 163, 268, 15);
					jLabelTrackDisplayMode.setFont(FONT);
					jLabelTrackDisplayMode.setPreferredSize(new java.awt.Dimension(261, 14));
				}
				{
					String[] keyNames = TrackMateModelView.TRACK_DISPLAY_MODE_DESCRIPTION;
					ComboBoxModel jComboBoxDisplayModeModel = new DefaultComboBoxModel(keyNames);
					jComboBoxDisplayMode = new JComboBox();
					jPanelTrackOptions.add(jComboBoxDisplayMode);
					jComboBoxDisplayMode.setModel(jComboBoxDisplayModeModel);
					jComboBoxDisplayMode.setSelectedIndex(0);
					jComboBoxDisplayMode.setFont(SMALL_FONT);
					jComboBoxDisplayMode.setPreferredSize(new java.awt.Dimension(265, 27));
					jComboBoxDisplayMode.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(TrackMateModelView<T> view : views) {
								view.setDisplaySettings(KEY_TRACK_DISPLAY_MODE, jComboBoxDisplayMode.getSelectedIndex());
								view.refresh();
							}
						}
					});
				}
				{
					jCheckBoxLimitDepth = new JCheckBox();
					jPanelTrackOptions.add(jCheckBoxLimitDepth);
					jCheckBoxLimitDepth.setText("Limit frame depth");
					jCheckBoxLimitDepth.setBounds(6, 216, 272, 23);
					jCheckBoxLimitDepth.setFont(FONT);
					jCheckBoxLimitDepth.setSelected(true);
					jCheckBoxLimitDepth.setPreferredSize(new java.awt.Dimension(259, 23));
					jCheckBoxLimitDepth.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							int depth;
							if (jCheckBoxLimitDepth.isSelected())
								depth = Integer.parseInt(jTextFieldFrameDepth.getText());
							else
								depth = (int) 1e9;
							for(TrackMateModelView<T> view : views) {
								view.setDisplaySettings(KEY_TRACK_DISPLAY_DEPTH, depth);
								view.refresh();
							}
						}
					});
				}
				{
					jLabelFrameDepth = new JLabel();
					jPanelTrackOptions.add(jLabelFrameDepth);
					jLabelFrameDepth.setText("  Frame depth:");
					jLabelFrameDepth.setFont(SMALL_FONT);
					jLabelFrameDepth.setPreferredSize(new java.awt.Dimension(103, 14));
				}
				{
					jTextFieldFrameDepth = new JTextField();
					jPanelTrackOptions.add(jTextFieldFrameDepth);
					jTextFieldFrameDepth.setFont(SMALL_FONT);
					jTextFieldFrameDepth.setText(""+TrackMateModelView.DEFAULT_TRACK_DISPLAY_DEPTH);
					jTextFieldFrameDepth.setPreferredSize(new java.awt.Dimension(34, 20));
					jTextFieldFrameDepth.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							int depth = Integer.parseInt(jTextFieldFrameDepth.getText());
							for(TrackMateModelView<T> view : views) {
								view.setDisplaySettings(KEY_TRACK_DISPLAY_DEPTH, depth);
								view.refresh();
							}
						}
					});
				}
			}
			{
				jCheckBoxDisplayTracks = new JCheckBox();
				mainPanel.add(jCheckBoxDisplayTracks);
				jCheckBoxDisplayTracks.setText("Display tracks");
				jCheckBoxDisplayTracks.setFont(FONT);
				jCheckBoxDisplayTracks.setBounds(10, 188, 233, 23);
				jCheckBoxDisplayTracks.setSelected(true);
				jCheckBoxDisplayTracks.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean isSelected = jCheckBoxDisplayTracks.isSelected();
						jPanelTrackOptions.setEnabled(isSelected);
						for(TrackMateModelView<T> view : views) {
							view.setDisplaySettings(KEY_TRACKS_VISIBLE, isSelected);
							view.refresh();
						}
					}
				});
			}
			{
				jCheckBoxDisplaySpots = new JCheckBox();
				mainPanel.add(jCheckBoxDisplaySpots);
				jCheckBoxDisplaySpots.setText("Display spots");
				jCheckBoxDisplaySpots.setFont(FONT);
				jCheckBoxDisplaySpots.setBounds(10, 38, 280, 23);
				jCheckBoxDisplaySpots.setSelected(true);
				jCheckBoxDisplaySpots.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean isSelected = jCheckBoxDisplaySpots.isSelected();
						jPanelSpotOptions.setEnabled(isSelected);
						for(TrackMateModelView<T> view : views) {
							view.setDisplaySettings(KEY_SPOTS_VISIBLE, isSelected);
							view.refresh();
						}
					}
				});
			}
			{
				jPanelSpotOptions = new JPanel() {
					private static final long serialVersionUID = 3259314983744108471L;
					public void setEnabled(boolean enabled) {
						for(Component c : getComponents())
							c.setEnabled(enabled);
					};
				};
				FlowLayout jPanelSpotOptionsLayout = new FlowLayout();
				jPanelSpotOptionsLayout.setAlignment(FlowLayout.LEFT);
				jPanelSpotOptions.setLayout(jPanelSpotOptionsLayout);
				mainPanel.add(jPanelSpotOptions);
				jPanelSpotOptions.setBounds(10, 63, 280, 110);
				jPanelSpotOptions.setBorder(new LineBorder(new java.awt.Color(192,192,192), 1, true));
				{
					JLabel jLabelSpotRadius = new JLabel();
					jLabelSpotRadius.setText("  Spot display radius ratio:");
					jLabelSpotRadius.setFont(SMALL_FONT);
					jPanelSpotOptions.add(jLabelSpotRadius);

					jTextFieldSpotRadius = new JNumericTextField("1");
					jTextFieldSpotRadius.setPreferredSize(new java.awt.Dimension(34, 20));
					jTextFieldSpotRadius.setFont(SMALL_FONT);
					jPanelSpotOptions.add(jTextFieldSpotRadius);
					jTextFieldSpotRadius.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(TrackMateModelView<T> view : views) {
								view.setDisplaySettings(KEY_SPOT_RADIUS_RATIO, (float) jTextFieldSpotRadius.getValue());
								view.refresh();
							}
						}
					});
					jTextFieldSpotRadius.addFocusListener(new FocusListener() {
						@Override
						public void focusLost(FocusEvent e) {
							for(TrackMateModelView<T> view : views) {
								view.setDisplaySettings(KEY_SPOT_RADIUS_RATIO, (float) jTextFieldSpotRadius.getValue());
								view.refresh();
							}							
						}
						@Override
						public void focusGained(FocusEvent e) {}
					});
				}
				{
					jCheckBoxDisplayNames = new JCheckBox();
					jCheckBoxDisplayNames.setText("Display spot names");
					jCheckBoxDisplayNames.setFont(SMALL_FONT);
					jCheckBoxDisplayNames.setSelected(false);
					jPanelSpotOptions.add(jCheckBoxDisplayNames);
					jCheckBoxDisplayNames.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for(TrackMateModelView<T> view : views) {
								view.setDisplaySettings(KEY_DISPLAY_SPOT_NAMES, jCheckBoxDisplayNames.isSelected());
								view.refresh();
							}
						}
					});
				}
				{
					Map<String, double[]> featureValues = model.getFeatureModel().getSpotFeatureValues();
					List<String> features = model.getFeatureModel().getSpotFeatures();
					Map<String, String> featureNames = model.getFeatureModel().getSpotFeatureNames();

					jPanelSpotColor = new JPanelColorByFeatureGUI(features, featureNames, mainPanel);
					jPanelSpotColor.setFeatureValues(featureValues);
					jPanelSpotOptions.add(jPanelSpotColor);
				}
			}
			{
				jLabelDisplayOptions = new JLabel();
				jLabelDisplayOptions.setText("Display options");
				jLabelDisplayOptions.setFont(BIG_FONT);
				jLabelDisplayOptions.setBounds(20, 11, 280, 20);
				jLabelDisplayOptions.setHorizontalAlignment(SwingConstants.LEFT);
				mainPanel.add(jLabelDisplayOptions);
			}
			{
				jButtonShowTrackScheme = new JButton();
				jButtonShowTrackScheme.setText("Track scheme");
				jButtonShowTrackScheme.setIcon(TRACK_SCHEME_ICON);
				jButtonShowTrackScheme.setFont(FONT);
				jButtonShowTrackScheme.setBounds(10, 345, 120, 30);
				jButtonShowTrackScheme.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						fireAction(TRACK_SCHEME_BUTTON_PRESSED);
					}
				});
				mainPanel.add(jButtonShowTrackScheme);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		getContentPane().add(mainPanel);
		setSize(300, 500);
		setResizable(false);
	}
	
}
