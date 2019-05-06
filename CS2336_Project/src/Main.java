/* 		CS 2336 Group Project
 * 		This class was created by (Multiple Contributers)
 */

import java.util.ArrayList;
import java.lang.Math;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.input.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.stage.*;
import javafx.util.Pair;

public class Main extends Application {
	ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
	ArrayList<Edge> edgeList = new ArrayList<Edge>();
	ArrayList<StackPane> vertexPanes = new ArrayList<StackPane>();
	int N;
	int values[][];
	private Pair<Double, Double> origin;
	private Canvas layer = new Canvas();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		Pane root = new Pane();

		Pane pane = new Pane();
		Pane instructions = new Pane();

		Rectangle instructionRect = new Rectangle(150, 100);
		instructionRect.setStroke(Color.BLACK);
		instructionRect.setFill(Color.TRANSPARENT);

		Label iLabel = new Label("INSTRUCTIONS\nAdd:\t\t\tLeft Click\n"
				+ "Move:\t\tCtrl Drag\nConnect:\t\tDrag\nRemove:\t\tRight Click");

		instructionRect.relocate(10, 10);
		iLabel.relocate(15, 15);

		instructions.getChildren().addAll(instructionRect, iLabel);

		HBox findSP = new HBox();

		Rectangle fspRect = new Rectangle(300, 75);
		fspRect.setStroke(Color.BLACK);
		fspRect.setFill(Color.TRANSPARENT);

		TextField sVertex = new TextField();
		TextField eVertex = new TextField();
		TextField sourceVertex = new TextField();
		sourceVertex.setPrefWidth(30);
		sVertex.setPrefWidth(30);
		eVertex.setPrefWidth(30);

		Button showMST = new Button("Show MST");
		Button showSP = new Button("Show Shortest Path");
		Button showAllSP = new Button("Show all SP from the source");

		findSP.getChildren().addAll(showMST, new Label("Source vertex:"), sourceVertex, showAllSP,
				new Label("Starting vertex: "), sVertex, new Label("Ending vertex: "), eVertex, showSP);

		instructions.setPadding(new Insets(100));
		findSP.setPadding(new Insets(15, 12, 15, 12));
		findSP.setSpacing(5);

		BorderPane border = new BorderPane();

		border.setTop(instructions);
		border.setBottom(findSP);

		root.getChildren().addAll(border, pane);

		Scene scene = new Scene(root, 750, 500);
		Canvas canvas = new Canvas();
		primaryStage.setScene(scene);

		showSP.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				int ending = Integer.parseInt(eVertex.getText());
				updateGraph();
				dijkstra(values, ending);
			}
		});

		showAllSP.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				int source = Integer.parseInt(sourceVertex.getText());
				showAllSP(source);
			}
		});

		scene.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() != MouseButton.PRIMARY)
					return;
				StackPane addVertex = createVertex(event);
				if (addVertex != null) {
					pane.getChildren().add(addVertex);
					addVertex.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event2) {
							if (event2.getButton() != MouseButton.SECONDARY)
								return;
							int place = inVertex(event2);
							if (place >= 0) {
								vertexList.remove(place);
								vertexPanes.remove(place);
								changeVerticiesNum(place);
								pane.getChildren().remove(addVertex);
							}
						}
					});
					addVertex.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event3) {
							if (!event.isControlDown())
								return;
							Circle circle2 = new Circle(event3.getSceneX(), event3.getSceneY(), 20);
						}
					});
				}
			}
		});

		scene.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (inVertex(event) != -1) {

					Canvas newLayer = new Canvas(750, 500);
					GraphicsContext context = newLayer.getGraphicsContext2D();
					drawLine(context);

					layer = newLayer;
					pane.getChildren().add(0, newLayer);
					origin = new Pair<>((double) vertexList.get(inVertex(event)).xLocation,
							(double) vertexList.get(inVertex(event)).yLocation);
					edgeList.add(new Edge(vertexList.get(inVertex(event))));
				}
			}
		});

		scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				GraphicsContext context = layer.getGraphicsContext2D();
				context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
				context.strokeLine(origin.getKey(), origin.getValue(), event.getSceneX(), event.getSceneY());
			}

		});

		scene.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (inVertex(event) == -1) {
					GraphicsContext context = layer.getGraphicsContext2D();
					context.clearRect(0, 0, layer.getWidth(), layer.getHeight());
					drawLineW(context);
					edgeList.remove(edgeList.size() - 1);
				} else {
					edgeList.get(edgeList.size() - 1).setEndpoint(vertexList.get(inVertex(event)));
					edgeList.get(edgeList.size() - 1).setLength();
				}

			}
		});

		pane.getChildren().add(canvas);
		primaryStage.show();
	}

	private void drawLine(GraphicsContext gc) {
		double canvasWidth = gc.getCanvas().getWidth();
		double canvasHeight = gc.getCanvas().getHeight();

		gc.fill();
		gc.strokeRect(0, 0, canvasWidth, canvasHeight);

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
	}

	private void drawLineW(GraphicsContext gc) {
		double canvasWidth = gc.getCanvas().getWidth();
		double canvasHeight = gc.getCanvas().getHeight();

		gc.fill();
		gc.strokeRect(0, 0, canvasWidth, canvasHeight);

		gc.setStroke(Color.WHITE);
		gc.setLineWidth(1);
	}

	public StackPane createVertex(MouseEvent event) {
		if (inVertex(event) == -1) {
			vertexList.add(new Vertex((int) event.getSceneX(), (int) event.getSceneY(), vertexList.size() - 1));
			Circle circle = new Circle(event.getSceneX(), event.getSceneY(), 20);
			circle.setStroke(Color.BLACK);
			circle.setFill(Color.TRANSPARENT);
			Text number = new Text(event.getSceneX(), event.getSceneY(), String.valueOf(vertexList.size() - 1));
			StackPane stack = new StackPane();
			stack.getChildren().addAll(circle, number);
			stack.relocate(event.getSceneX() - 20, event.getSceneY() - 20);
			vertexPanes.add(stack);
			return stack;
		}
		return null;
	}

	public int inVertex(MouseEvent event) {
		if (vertexList.size() == 0)
			return -1;
		else {
			int check = 0;
			int hold = -1;
			while (check < vertexList.size()) {
				double distance = Math.hypot(event.getSceneX() - vertexList.get(check).xLocation,
						event.getSceneY() - vertexList.get(check).yLocation);
				if (distance <= 20.0)
					return check;
				else if (distance < 40.0)
					hold = check;
				check++;
			}
			if (hold > -1)
				return -2;
			return -1;
		}
	}

	public void changeVerticiesNum(int n) {
		if (n != vertexList.size()) {
			for (int i = n; i < vertexList.size(); i++) {
				vertexPanes.get(i).getChildren().remove(1);
				vertexList.get(i).setVertexNumLower();
				String t = Integer.toString(vertexList.get(i).num + 1);
				vertexPanes.get(i).getChildren().add(new Text(t));
			}
		}
	}

	public void showAllSP(int source) {
		Vertex[] vertexGraph = new Vertex[vertexList.size()];
		int[][] edgeGraph = new int[vertexList.size()][3];
		for (int i = 0; i < vertexList.size(); i++) {
			vertexGraph[i] = vertexList.get(i);
		}
		for (int i = 0; i < edgeList.size(); i++) {
			edgeGraph[i][0] = vertexList.indexOf(edgeList.get(i).vertex1);
			edgeGraph[i][1] = vertexList.indexOf(edgeList.get(i).vertex2);
			edgeGraph[i][2] = (int) edgeList.get(i).length;
		}
		WeightedGraph<Vertex> graph = new WeightedGraph<>(vertexGraph, edgeGraph);
		WeightedGraph<Vertex>.ShortestPathTree tree1 = graph.getShortestPath(graph.getIndex(vertexList.get(source)));
		dijkstra(edgeGraph, source);
	}

	public void updateGraph() {

		int n = vertexList.size();
		values = new int[n][n];

		for (int j = 0; j < edgeList.size(); j++) {
			values[edgeList.get(j).vertex1.num][edgeList.get(j).vertex2.num] = (int) edgeList.get(j).length;
		}

		N = n;

	}

	int minDistance(int dist[], Boolean sptSet[]) {
		int min = Integer.MAX_VALUE, min_index = -1;

		for (int v = 0; v < N; v++)
			if (sptSet[v] == false && dist[v] <= min) {
				min = dist[v];
				min_index = v;
			}

		return min_index;
	}

	void dijkstra(int graph[][], int src) {
		int dist[] = new int[N];
		Boolean sptSet[] = new Boolean[N];
		ArrayList<Edge> SP = new ArrayList<Edge>();

		for (int i = 0; i < N; i++) {
			dist[i] = Integer.MAX_VALUE;
			sptSet[i] = false;
		}

		dist[src] = 0;

		for (int count = 0; count < N - 1; count++) {
			int u = minDistance(dist, sptSet);

			sptSet[u] = true;
			for (int v = 0; v < N; v++)
				if (!sptSet[v] && graph[u][v] != 0 && dist[u] != Integer.MAX_VALUE && dist[u] + graph[u][v] < dist[v]) {
					dist[v] = dist[u] + graph[u][v];
				}
		}

		// printSolution(dist, N);
		for (int k = 0; k < dist.length; k++) {

			for (int l = 0; l < edgeList.size(); l++) {

				if (edgeList.get(l).vertex1.num == dist[k]) {

					for (int m = 0; m < edgeList.size(); m++) {

						if (edgeList.get(l).vertex2.num == dist[k + 1]) {

							SP.add(edgeList.get(l));
						}
					}
				}
			}
		}

		for (int i = 0; i < 5; i++) {
			System.out.println("Im here 2");
			System.out.println(SP.get(i).toString());
		}
	}

}
