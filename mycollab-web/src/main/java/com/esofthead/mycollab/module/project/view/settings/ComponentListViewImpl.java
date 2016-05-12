/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.project.view.settings;

import com.esofthead.mycollab.common.TableViewField;
import com.esofthead.mycollab.common.i18n.GenericI18Enum;
import com.esofthead.mycollab.common.i18n.OptionI18nEnum.StatusI18nEnum;
import com.esofthead.mycollab.module.project.CurrentProjectVariables;
import com.esofthead.mycollab.module.project.ProjectLinkBuilder;
import com.esofthead.mycollab.module.project.ProjectRolePermissionCollections;
import com.esofthead.mycollab.module.project.ProjectTooltipGenerator;
import com.esofthead.mycollab.module.project.i18n.ComponentI18nEnum;
import com.esofthead.mycollab.module.project.view.settings.component.ProjectUserLink;
import com.esofthead.mycollab.module.tracker.domain.SimpleComponent;
import com.esofthead.mycollab.module.tracker.domain.criteria.ComponentSearchCriteria;
import com.esofthead.mycollab.module.tracker.service.ComponentService;
import com.esofthead.mycollab.spring.AppContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.events.HasMassItemActionHandler;
import com.esofthead.mycollab.vaadin.events.HasSearchHandlers;
import com.esofthead.mycollab.vaadin.events.HasSelectableItemHandlers;
import com.esofthead.mycollab.vaadin.events.HasSelectionOptionHandlers;
import com.esofthead.mycollab.vaadin.mvp.AbstractPageView;
import com.esofthead.mycollab.vaadin.mvp.ViewComponent;
import com.esofthead.mycollab.vaadin.ui.*;
import com.esofthead.mycollab.vaadin.web.ui.*;
import com.esofthead.mycollab.vaadin.web.ui.table.AbstractPagedBeanTable;
import com.esofthead.mycollab.vaadin.web.ui.table.DefaultPagedBeanTable;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import org.vaadin.viritin.layouts.MHorizontalLayout;

import java.util.Arrays;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
@ViewComponent
public class ComponentListViewImpl extends AbstractPageView implements ComponentListView {
    private static final long serialVersionUID = 1L;

    private ComponentSearchPanel componentSearchPanel;
    private SelectionOptionButton selectOptionButton;
    private DefaultPagedBeanTable<ComponentService, ComponentSearchCriteria, SimpleComponent> tableItem;
    private VerticalLayout componentListLayout;
    private DefaultMassItemActionHandlerContainer tableActionControls;
    private Label selectedItemsNumberLabel = new Label();

    public ComponentListViewImpl() {
        this.setMargin(new MarginInfo(false, true, true, true));

        this.componentSearchPanel = new ComponentSearchPanel();
        this.addComponent(this.componentSearchPanel);

        componentListLayout = new VerticalLayout();
        this.addComponent(componentListLayout);

        this.generateDisplayTable();
    }

    private void generateDisplayTable() {
        tableItem = new DefaultPagedBeanTable<>(AppContextUtil.getSpringBean(ComponentService.class),
                SimpleComponent.class, new TableViewField(null, "selected", UIConstants.TABLE_CONTROL_WIDTH),
                Arrays.asList(
                        new TableViewField(GenericI18Enum.FORM_NAME, "componentname", UIConstants.TABLE_EX_LABEL_WIDTH),
                        new TableViewField(ComponentI18nEnum.FORM_LEAD, "userLeadFullName", UIConstants.TABLE_X_LABEL_WIDTH),
                        new TableViewField(GenericI18Enum.FORM_STATUS, "status", UIConstants.TABLE_M_LABEL_WIDTH),
                        new TableViewField(GenericI18Enum.FORM_DESCRIPTION, "description", 500),
                        new TableViewField(GenericI18Enum.FORM_PROGRESS, "id", UIConstants.TABLE_EX_LABEL_WIDTH)));

        tableItem.addGeneratedColumn("selected", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                final SimpleComponent component = tableItem.getBeanByIndex(itemId);
                CheckBoxDecor cb = new CheckBoxDecor("", component.isSelected());
                cb.setImmediate(true);
                cb.addValueChangeListener(new Property.ValueChangeListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        tableItem.fireSelectItemEvent(component);

                    }
                });

                component.setExtraData(cb);
                return cb;
            }
        });

        tableItem.addGeneratedColumn("componentname", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public com.vaadin.ui.Component generateCell(Table source, Object itemId, Object columnId) {
                SimpleComponent bugComponent = tableItem.getBeanByIndex(itemId);
                LabelLink b = new LabelLink(bugComponent.getComponentname(), ProjectLinkBuilder
                        .generateComponentPreviewFullLink(bugComponent.getProjectid(), bugComponent.getId()));
                if (bugComponent.getStatus() != null && bugComponent.getStatus().equals(StatusI18nEnum.Closed.name())) {
                    b.addStyleName(UIConstants.LINK_COMPLETED);
                }
                b.setDescription(ProjectTooltipGenerator.generateToolTipComponent(AppContext.getUserLocale(),
                        bugComponent, AppContext.getSiteUrl(), AppContext.getUserTimeZone()));
                return b;

            }
        });

        tableItem.addGeneratedColumn("userLeadFullName", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public com.vaadin.ui.Component generateCell(final Table source, final Object itemId, final Object columnId) {
                SimpleComponent bugComponent = tableItem.getBeanByIndex(itemId);
                return new ProjectUserLink(bugComponent.getUserlead(),
                        bugComponent.getUserLeadAvatarId(), bugComponent.getUserLeadFullName());

            }
        });

        tableItem.addGeneratedColumn("id", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 1L;

            @Override
            public com.vaadin.ui.Component generateCell(final Table source, final Object itemId, final Object columnId) {
                SimpleComponent bugComponent = tableItem.getBeanByIndex(itemId);
                return new ProgressBarIndicator(bugComponent.getNumBugs(), bugComponent.getNumOpenBugs(), false);

            }
        });

        tableItem.setWidth("100%");
        componentListLayout.addComponent(constructTableActionControls());
        componentListLayout.addComponent(tableItem);
    }

    @Override
    public HasSearchHandlers<ComponentSearchCriteria> getSearchHandlers() {
        return this.componentSearchPanel;
    }

    private ComponentContainer constructTableActionControls() {
        final CssLayout layoutWrapper = new CssLayout();
        layoutWrapper.setWidth("100%");

        MHorizontalLayout layout = new MHorizontalLayout();
        layoutWrapper.addStyleName(UIConstants.TABLE_ACTION_CONTROLS);
        layoutWrapper.addComponent(layout);

        this.selectOptionButton = new SelectionOptionButton(tableItem);
        layout.addComponent(this.selectOptionButton);

        tableActionControls = new DefaultMassItemActionHandlerContainer();
        if (CurrentProjectVariables.canAccess(ProjectRolePermissionCollections.COMPONENTS)) {
            tableActionControls.addDeleteActionItem();
        }

        tableActionControls.addMailActionItem();
        tableActionControls.addDownloadPdfActionItem();
        tableActionControls.addDownloadExcelActionItem();
        tableActionControls.addDownloadCsvActionItem();

        layout.with(tableActionControls, selectedItemsNumberLabel)
                .withAlign(selectedItemsNumberLabel, Alignment.MIDDLE_CENTER);
        return layoutWrapper;
    }

    @Override
    public void showNoItemView() {
        removeAllComponents();
        this.addComponent(new ComponentListNoItemView());
    }

    @Override
    public void enableActionControls(final int numOfSelectedItems) {
        tableActionControls.setVisible(true);
        this.selectedItemsNumberLabel.setValue(AppContext.getMessage(
                GenericI18Enum.TABLE_SELECTED_ITEM_TITLE, numOfSelectedItems));
    }

    @Override
    public void disableActionControls() {
        tableActionControls.setVisible(false);
        this.selectOptionButton.setSelectedCheckbox(false);
        this.selectedItemsNumberLabel.setValue("");
    }

    @Override
    public HasSelectionOptionHandlers getOptionSelectionHandlers() {
        return this.selectOptionButton;
    }

    @Override
    public HasMassItemActionHandler getPopupActionHandlers() {
        return tableActionControls;
    }

    @Override
    public HasSelectableItemHandlers<SimpleComponent> getSelectableItemHandlers() {
        return tableItem;
    }

    @Override
    public AbstractPagedBeanTable<ComponentSearchCriteria, SimpleComponent> getPagedBeanTable() {
        return tableItem;
    }
}
