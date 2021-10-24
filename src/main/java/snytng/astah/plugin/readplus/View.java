package snytng.astah.plugin.readplus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IActivityDiagram;
import com.change_vision.jude.api.inf.model.IBlockDefinitionDiagram;
import com.change_vision.jude.api.inf.model.IClassDiagram;
import com.change_vision.jude.api.inf.model.ICommunicationDiagram;
import com.change_vision.jude.api.inf.model.IDataFlowDiagram;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IRequirementDiagram;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.model.IStateMachineDiagram;
import com.change_vision.jude.api.inf.model.IUseCaseDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import com.change_vision.jude.api.inf.project.ProjectEvent;
import com.change_vision.jude.api.inf.project.ProjectEventListener;
import com.change_vision.jude.api.inf.ui.IPluginExtraTabView;
import com.change_vision.jude.api.inf.ui.ISelectionListener;
import com.change_vision.jude.api.inf.view.IDiagramEditorSelectionEvent;
import com.change_vision.jude.api.inf.view.IDiagramEditorSelectionListener;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;
import com.change_vision.jude.api.inf.view.IEntitySelectionEvent;
import com.change_vision.jude.api.inf.view.IEntitySelectionListener;
import com.change_vision.jude.api.inf.view.IProjectViewManager;

public class View
extends
JPanel
implements
IPluginExtraTabView,
IEntitySelectionListener,
IDiagramEditorSelectionListener,
ProjectEventListener,
ListSelectionListener
{
	/**
	 * プロパティファイルの配置場所
	 */
	private static final String VIEW_PROPERTIES = View.class.getPackage().getName() + ".view";
	private static final String VIEW_UML_PROPERTIES = View.class.getPackage().getName() + ".view_uml";
	private static final String VIEW_SYSML_PROPERTIES = View.class.getPackage().getName() + ".view_sysml";

	/**
	 * リソースバンドル
	 */
	private static final ResourceBundle VIEW_BUNDLE = ResourceBundle.getBundle(VIEW_PROPERTIES, Locale.getDefault());
	private static final ResourceBundle VIEW_UML_BUNDLE = ResourceBundle.getBundle(VIEW_UML_PROPERTIES, Locale.getDefault());
	private static final ResourceBundle VIEW_SYSML_BUNDLE = ResourceBundle.getBundle(VIEW_SYSML_PROPERTIES, Locale.getDefault());
	private static Properties props = new Properties();

	static {
		try {
			Path file = Paths.get(System.getProperty("user.home"), "readplus.view.properties");
			props.load(new FileInputStream(file.toFile()));;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static final String getViewString(String key) {
		if (props.containsKey(key)) {
			return (String)props.get(key);

		} else if (VIEW_BUNDLE.containsKey(key)) {
			return VIEW_BUNDLE.getString(key);
		} else if (VIEW_UML_BUNDLE.containsKey(key)) {
			return VIEW_UML_BUNDLE.getString(key);
		} else if (VIEW_SYSML_BUNDLE.containsKey(key)) {
			return VIEW_SYSML_BUNDLE.getString(key);

		} else {
			return String.format("<%s>", key);
		}
	}

	private String title = "<Read Diagrams>";
	private String description = "<This plugin reads diagrams in the project.>";

	private static final long serialVersionUID = 1L;
	private transient ProjectAccessor projectAccessor = null;
	private transient IDiagramViewManager diagramViewManager = null;
	private transient IProjectViewManager projectViewManager = null;

	public View() {
		try {
			projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
			diagramViewManager = projectAccessor.getViewManager().getDiagramViewManager();
			projectViewManager = projectAccessor.getViewManager().getProjectViewManager();
		} catch (ClassNotFoundException | InvalidUsingException e){
			e.printStackTrace();
		}

		initProperties();

		initComponents();
	}

	private void initProperties() {
		try {
			title       = VIEW_BUNDLE.getString("pluginExtraTabView.title");
			description = VIEW_BUNDLE.getString("pluginExtraTabView.description");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		add(createLabelPane(), BorderLayout.CENTER);
		add(createOperationPanel(), BorderLayout.EAST);
	}

	private void addDiagramListeners(){
		diagramViewManager.addDiagramEditorSelectionListener(this);
		diagramViewManager.addEntitySelectionListener(this);
		projectViewManager.addEntitySelectionListener(this);
		projectAccessor.addProjectEventListener(this);
	}

	private void removeDiagaramListeners(){
		diagramViewManager.removeDiagramEditorSelectionListener(this);
		diagramViewManager.removeEntitySelectionListener(this);
		projectViewManager.removeEntitySelectionListener(this);
		projectAccessor.removeProjectEventListener(this);
	}

	JList<String> textArea = null;
	JScrollPane scrollPane = null;
	private Container createLabelPane() {
		textArea = new JList<>(new String[]{});
		textArea.addListSelectionListener(this);

		scrollPane = new JScrollPane(textArea);
		return scrollPane;
	}

	JLabel optionLabel = new JLabel(View.getViewString("View.optionLabel"));
	JLabel readTargetLabel = new JLabel(View.getViewString("View.readTargetLabel"));
	String[] readTargetButtonNames = View.getViewString("View.readTaregetButton").split(",");
	JToggleButton readTaregetButton = new JToggleButton(readTargetButtonNames[0]);
	JLabel operationLabel = new JLabel(View.getViewString("View.operationLabel"));
	String[] comboData = View.getViewString("View.comboData").split(",");
	JComboBox<String> operationComboBox = new JComboBox<>(comboData);
	JLabel zoomLabel = new JLabel(View.getViewString("View.zoomLabel"));
	JSlider zoomSlider = new JSlider(10, 100);

	private JPanel createOperationPanel() {
		readTargetLabel.setVerticalAlignment(JLabel.BOTTOM);
		readTargetLabel.setAlignmentX(LEFT_ALIGNMENT);
		readTaregetButton.addActionListener(e -> updateDiagramView());
		readTaregetButton.setAlignmentX(LEFT_ALIGNMENT);

		operationLabel.setVerticalAlignment(JLabel.BOTTOM);
		operationLabel.setAlignmentX(LEFT_ALIGNMENT);
		operationComboBox.setAlignmentX(LEFT_ALIGNMENT);

		zoomLabel.setVerticalAlignment(JLabel.BOTTOM);
		zoomLabel.setAlignmentX(LEFT_ALIGNMENT);
		zoomSlider.setAlignmentX(LEFT_ALIGNMENT);
		zoomSlider.addChangeListener(e -> {
			int zoom = zoomSlider.getValue();
			Font f = textArea.getFont();
			textArea.setFont(new Font(f.getName(), f.getStyle(), zoom));
		});

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(readTargetLabel);
		p.add(readTaregetButton);
		p.add(Box.createGlue());
		p.add(operationLabel);
		p.add(operationComboBox);
		p.add(Box.createGlue());
		p.add(zoomLabel);
		p.add(zoomSlider);

		JPanel op = new JPanel();
		op.setLayout(new BorderLayout());
		optionLabel.setHorizontalAlignment(JLabel.CENTER);
		op.add(optionLabel, BorderLayout.NORTH);
		op.add(p, BorderLayout.CENTER);

		return op;
	}

	/**
	 * 図の選択が変更されたら表示を更新する
	 */
	@Override
	public void diagramSelectionChanged(IDiagramEditorSelectionEvent e) {
		updateDiagramView();
	}

	/**
	 * 要素の選択が変更されたら表示を更新する
	 */
	@Override
	public void entitySelectionChanged(IEntitySelectionEvent e) {
		updateDiagramView();
	}

	// 読み上げ結果
	private transient MessagePresentation messagePresentation = null;

	/**
	 * 表示を更新する
	 */
	private void updateDiagramView(){
		try {
			// 今選択している図のタイプを取得する
			IDiagram diagram = diagramViewManager.getCurrentDiagram();

			if(readTaregetButton.isSelected()){
				readTaregetButton.setText(readTargetButtonNames[1]);
				diagram = null;
			} else {
				readTaregetButton.setText(readTargetButtonNames[0]);
			}

			// メッセージとプレゼンテーションをリセット
			messagePresentation = null;

			// 選択している図を読み上げる

			// 選択している図がクラス図ならば、クラス図に含まれるクラス数を表示
			if(diagram instanceof IClassDiagram){
				messagePresentation = ClassDiagramReader.getMessagePresentation((IClassDiagram)diagram, diagramViewManager);
			}
			// 選択している図がコミュニケーション図ならば、コミュニケーション図の手順を表示
			else if(diagram instanceof ICommunicationDiagram){
				messagePresentation = CommunicationDiagramReader.getMessagePresentation((ICommunicationDiagram)diagram, diagramViewManager);
			}
			// 選択している図がシーケンス図ならば、シーケンス図の手順を表示
			else if(diagram instanceof ISequenceDiagram){
				messagePresentation = SequenceDiagramReader.getMessagePresentation((ISequenceDiagram)diagram, diagramViewManager);
			}
			// 選択している図が状態マシン図ならば、状態マシン図の手順を表示
			else if(diagram instanceof IStateMachineDiagram){
				messagePresentation = StateMachineDiagramReader.getMessagePresentation((IStateMachineDiagram)diagram, diagramViewManager);
			}
			// 選択している図がアクティビティ図ならば、アクティビティを読み上げ
			else if(diagram instanceof IActivityDiagram){
				messagePresentation = ActivityDiagramReader.getMessagePresentation((IActivityDiagram)diagram, diagramViewManager);
			}
			// 選択している図がデータフロー図ならば、プロセスを読み上げ
			else if(diagram instanceof IDataFlowDiagram){
				messagePresentation = DataFlowDiagramReader.getMessagePresentation((IDataFlowDiagram)diagram, diagramViewManager);
			}
			// 選択している図がユースケース図ならば、プロセスを読み上げ
			else if(diagram instanceof IUseCaseDiagram){
				messagePresentation = UseCaseDiagramReader.getMessagePresentation((IUseCaseDiagram)diagram, diagramViewManager);
			}
			// 選択している図がブロック定義図ならば、ブロックを読み上げ
			else if(diagram instanceof IBlockDefinitionDiagram){
				messagePresentation = BlockDefinitionDiagramReader.getMessagePresentation((IBlockDefinitionDiagram)diagram, diagramViewManager);
			}
			// 選択している図が要求図ならば、要求を読み上げ
			else if(diagram instanceof IRequirementDiagram){
				messagePresentation = RequirementDiagramReader.getMessagePresentation((IRequirementDiagram)diagram, diagramViewManager);
			}
			// それ以外はプロジェクトを読み上げる
			else {
				messagePresentation = AstahProjectReader.getMessagePresentation(diagramViewManager);
			}

			// メッセージのリスト化
			textArea.setListData(messagePresentation.getMessagesArray());

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	// IPluginExtraTabView
	@Override
	public void addSelectionListener(ISelectionListener listener) {
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void activated() {
		addDiagramListeners();
		updateDiagramView();
	}

	@Override
	public void deactivated() {
		removeDiagaramListeners();
	}

	// ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()){ // 操作中
			return;
		}

		int index = textArea.getSelectedIndex();
		System.out.println("textArea selected index=" + index);

		if(index < 0){ // 選択項目がない場合は-1なので処理しない
			return;
		}

		if(messagePresentation.presentations != null){
			removeDiagaramListeners();

			try {
				clearAllViewProperties();

				IPresentation sp = messagePresentation.presentations.get(index);
				if(sp != null){
					System.out.println("textArea presentation=" + sp);

					if(operationComboBox.getSelectedIndex() != 2){
						diagramViewManager.select(sp); // 選択する
						setPresentationView(sp, Color.MAGENTA); //　色を付ける
						if(operationComboBox.getSelectedIndex() != 1){
							diagramViewManager.showInDiagramEditor(sp); // 中心に持ってくる
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}

			addDiagramListeners();
		}

	}

	private void clearAllViewProperties(){
		// 選択図のビュープロパティーをクリアする
		try {
			diagramViewManager.clearAllViewProperties(diagramViewManager.getCurrentDiagram());
		} catch (InvalidUsingException e) {
			e.printStackTrace();
		}
	}



	private void setPresentationView(IPresentation p, Color c){
		//try {
			diagramViewManager.setViewProperty(p, IDiagramViewManager.BACKGROUND_COLOR, c);
			diagramViewManager.setViewProperty(p, IDiagramViewManager.BORDER_COLOR, c);
			diagramViewManager.setViewProperty(p, IDiagramViewManager.LINE_COLOR, c);
		//} catch(InvalidUsingException e){
		//	e.printStackTrace();
		//}
	}

	@Override
	public void projectChanged(ProjectEvent arg0) {
		updateDiagramView();
	}

	@Override
	public void projectClosed(ProjectEvent arg0) {
		// no action
	}

	@Override
	public void projectOpened(ProjectEvent arg0) {
		// no action
	}

}
