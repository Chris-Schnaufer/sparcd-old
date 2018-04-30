package controller.analysisView.conditions;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import model.SanimalData;
import model.query.IQueryCondition;
import model.query.QueryEngine;
import model.query.conditions.EndDateCondition;
import model.query.conditions.LocationFilterCondition;
import model.query.conditions.SpeciesFilterCondition;
import model.query.conditions.StartDateCondition;

import java.net.URL;
import java.util.ResourceBundle;

public class AddQueryConditionController implements IConditionController
{
	///
	/// FXML Bound Fields Start
	///

	@FXML
	public Button btnAdd;
	@FXML
	public ListView<QueryEngine.QueryFilters> lvwConditions;
	@FXML
	public Button btnReset;

	///
	/// FXML Bound Fields End
	///

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		lvwConditions.setItems(SanimalData.getInstance().getQueryEngine().getQueryFilters());
		this.lvwConditions.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			ObservableList<IQueryCondition> queryConditions = SanimalData.getInstance().getQueryEngine().getQueryConditions();
			if (newValue != null)
				queryConditions.add(queryConditions.size() - 1, newValue.createInstance());
			this.btnAdd.setVisible(true);
			this.btnReset.setVisible(false);
			this.lvwConditions.setVisible(false);
		});
	}

	@Override
	public void initializeData(IQueryCondition queryCondition)
	{
	}

	public void addNewCondition(ActionEvent actionEvent)
	{
		this.btnAdd.setVisible(false);
		this.btnReset.setVisible(true);
		this.lvwConditions.getSelectionModel().clearSelection();
		this.lvwConditions.setVisible(true);
	}

	public void resetToAdd(ActionEvent actionEvent)
	{
		this.btnAdd.setVisible(true);
		this.btnReset.setVisible(false);
		this.lvwConditions.setVisible(false);
	}
}