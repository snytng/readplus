package snytng.astah.plugin.readplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IElement;
import com.change_vision.jude.api.inf.model.IInteraction;
import com.change_vision.jude.api.inf.model.ILifeline;
import com.change_vision.jude.api.inf.model.IMessage;
import com.change_vision.jude.api.inf.model.IOperation;
import com.change_vision.jude.api.inf.model.IParameter;
import com.change_vision.jude.api.inf.model.ISequenceDiagram;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import com.change_vision.jude.api.inf.view.IDiagramViewManager;

/**
 * シーケンス図の関連名を読み上げる
 */
public class SequenceDiagramReader {

	private ISequenceDiagram diagram = null;

	public SequenceDiagramReader(ISequenceDiagram diagram) {
		this.diagram = diagram;
	}

	/**
	 * シーケンス図に含まれるライフライン数を取得する
	 * @return ライフライン数
	 */
	public int getNumberOfLifelines(){
		IInteraction interaction = diagram.getInteraction();
		return interaction == null ? 0 : interaction.getLifelines().length;
	}

	/**
	 * シーケンス図に含まれるメッセージ数を取得する
	 * @return メッセージ数
	 */
	public int getNumberOfMessages(){
		IInteraction interaction = diagram.getInteraction();
		return interaction == null ? 0 : interaction.getMessages().length;
	}

	public String read(IPresentation p){
		String message = "";

		if(p.getModel() instanceof IMessage){
			IMessage m = (IMessage)p.getModel();

			// インデックス
			String index = m.getIndex();

			// インデックスに応じた段下げ
			String indent = String.join("", Collections.nCopies(index.split("\\.").length, "  "));

			// メッセージの送信者
			String source = "";
			ILifeline s = null;
			if(m.getSource() instanceof ILifeline){
				s = (ILifeline)m.getSource();
				if(s.getBase() != null){
					source = String.format(View.getViewString("SequenceDiagramReader.Lifeline.nameWithClass"), s.getName(), s.getBase().getName());
				} else {
					source = String.format(View.getViewString("SequenceDiagramReader.Lifeline.nameWithoutClass"), s.getName());
				}
			}

			// メッセージの受信者
			String target = "";
			ILifeline t = null;
			if(m.getTarget() instanceof ILifeline){
				t = (ILifeline)m.getTarget();
				if(t.getBase() != null){
					target = String.format(View.getViewString("SequenceDiagramReader.Lifeline.nameWithClass"), t.getName(), t.getBase().getName());
				} else {
					target = String.format(View.getViewString("SequenceDiagramReader.Lifeline.nameWithoutClass"), t.getName());
				}
			}

			// メッセージ名
			String messageName = m.getName();

			// メッセージパラメーター
			String messageParameters = "(";

			// メッセージの引数があればそちらを優先し、なければ関数の引数を利用(astah*仕様)
			if(! m.getArgument().equals("")){
				messageParameters += m.getArgument();
			}
			else
				if(m.getOperation() != null){
					IParameter[] params = m.getOperation().getParameters();
					String paramstrings =
							Arrays.stream(params)
							.map(IParameter::getName)
							.collect(Collectors.joining(", "));
					messageParameters += paramstrings;
				}
			messageParameters += ")";

			// クラスに定義されたOperationになっているかどうかを表示
			IOperation operation = m.getOperation();
			String[] oxData = View.getViewString("SequenceDiagramReader.operation.ox").split(",");
			String ox = operation != null ? oxData[0]: oxData[1];

			// メッセージを読み上げ
			String operationMessage = String.format(
					View.getViewString("SequenceDiagramReader.operation.message"),
					source, target, messageName);
			message += String.format(
					"%s%s: %s%s %s",
					indent, index, operationMessage, messageParameters, ox);

		}

		return message;
	}

	private IPresentation[] sort(IPresentation[] presentations){
		// ソートされたIPresentation
		List<IPresentation> sps = new ArrayList<>();


		// 全てのインタラクションを取得
		IInteraction interaction = diagram.getInteraction();
		if(interaction != null){
			IMessage[] messages = interaction.getMessages();
			// メッセージをインデックス順にソート
			List<IMessage> vm = new ArrayList<>();
			for(IMessage m: messages){
				vm.add(m);
			}
			vm.sort((o1, o2) -> {
					StringTokenizer st1 = new StringTokenizer(o1.getIndex(), ".");
					StringTokenizer st2 = new StringTokenizer(o2.getIndex(), ".");

					while(st1.hasMoreTokens()){
						if(st2.hasMoreTokens()){
							int i1 = Integer.parseInt(st1.nextToken());
							int i2 = Integer.parseInt(st2.nextToken());
							if(i1 < i2){
								return -1;
							} else if(i1 > i2){
								return 1;
							}
						} else {
							return 1;
						}
					}
					if(st2.hasMoreTokens()){
						return -1;
					} else {
						return 0;
					}
				});


			// IPresentation -> IElementへ変換
			List<IPresentation> ps = Arrays.asList(presentations);
			List<IElement> es = ps.stream().map(IPresentation::getModel).collect(Collectors.toList());

			// メッセージを表示
			for(IMessage m: vm){

				// メッセージの送信者
				ILifeline s = null;
				if(m.getSource() instanceof ILifeline){
					s = (ILifeline)m.getSource();
				}

				// メッセージの受信者
				ILifeline t = null;
				if(m.getTarget() instanceof ILifeline){
					t = (ILifeline)m.getTarget();
				}

				// メッセージ表示を判断
				if(es.contains(s) || es.contains(t) || es.contains(m)){
					sps.add(ps.get(es.indexOf(m)));
				}

			}
		}

		return sps.toArray(new IPresentation[sps.size()]);
	}



	public static MessagePresentation getMessagePresentation(ISequenceDiagram diagram, IDiagramViewManager dvm) {
		MessagePresentation mps = new MessagePresentation();

		SequenceDiagramReader sdr = new SequenceDiagramReader(diagram);

		// ライフライン数を表示する
		String diagramMessage = String.format(
				View.getViewString("SequenceDiagramReader.diagram.message"),
				diagram.getName(), sdr.getNumberOfLifelines(), sdr.getNumberOfMessages()
				);
		mps.add(diagramMessage, null);

		mps.add("=====", null);

		// 選択要素の表示
		IPresentation[] ps = dvm.getSelectedPresentations();
		if(ps.length > 0){
			String selectionMessage = String.format(
					View.getViewString("SequenceDiagramReader.selection.message"),
					diagram.getName(), ps.length
					);
			mps.add(selectionMessage, null);

			// 選択した要素をソートする
			IPresentation[] sps = sdr.sort(ps);

			// 選択された要素のコミュニケーション手順を表示する
			for(IPresentation p : sps){
				String m = sdr.read(p);
				if(! m.equals("")){
					mps.add(m, p);
					System.out.println("selected " + m + ", " + p.toString());
				}
			}

		}
		else
		{
			// 全てのコミュニケーション手順を表示する

			// Presentationをソート
			IPresentation[] sps;
			try {
				sps = sdr.sort(diagram.getPresentations());
				// 選択された要素のコミュニケーション手順を表示する
				for(IPresentation p : sps){
					String m = sdr.read(p);
					if(! m.equals("")){
						mps.add(m, p);
						System.out.println("all " + m + "," + p.toString());
					}
				}
			} catch (InvalidUsingException e) {
				e.printStackTrace();
			}

		}

		return mps;
	}


}
