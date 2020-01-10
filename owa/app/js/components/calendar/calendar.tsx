/* * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
import React from 'react';
import { connect } from 'react-redux';
import Calendar from '@bit/soldevelo-omrs.cfl-components.calendar';
import {
  Col,
  Row,
  Button,
  Checkbox
} from 'react-bootstrap';
import { Tabs, Tab } from 'react-bootstrap'
import { IRootState } from '../../reducers';
import { getServiceResultLists } from '../../reducers/calendar.reducer'
import { ServiceStatus } from '../../shared/enums/service-status';
import { ServiceResultListUI } from '../../shared/model/service-result-list-ui';
import moment, { Moment } from 'moment';
import {
  getTemplates
} from '../../reducers/patient-template.reducer'
import { TemplateUI } from '../../shared/model/template-ui';
import _ from 'lodash';
import { RouteComponentProps } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getActorList } from '../../reducers/actor.reducer'

interface ICalendarViewProps extends DispatchProps, StateProps, RouteComponentProps<{ patientId: string }> {
};

interface ICalendarViewState {
  startDate?: Moment;
  endDate?: Moment;
  activeTabKey?: string;
  filters: Object;
  patientId: number;
}

interface ICalendarMessageEvent {
  serviceName: string;
  status: ServiceStatus;
  executionDate: Moment;
  messageId: Object;
}

interface IActorIdWithEvents {
  actorId: number;
  actorDisplayName: string;
  events: Array<ICalendarMessageEvent>;
}


class CalendarView extends React.Component<ICalendarViewProps, ICalendarViewState> {
  constructor(props: ICalendarViewProps) {
    super(props);
    this.state = {
      startDate: undefined,
      endDate: undefined,
      activeTabKey: 'Patient',
      filters: {},
      patientId: parseInt(this.props.match.params.patientId, 10)
    };
  }

  componentDidMount() {
    this.props.getTemplates();
    this.props.getActorList(this.state.patientId);
  }

  componentWillUpdate(nextProps: ICalendarViewProps, nextState: ICalendarViewState) {
    if (nextProps.loading === false && this.props.loading === true) {
      let initialFilters = {};
      nextProps.templates.forEach((template) => initialFilters[template.name as string] = true);
      this.setState({ filters: initialFilters });
    }
  }

  private dateRangeChanged = (start: Date, end: Date, tabKey: string) => {
    if (this.state.patientId && tabKey === this.state.activeTabKey &&
      ((this.state.startDate && this.state.startDate.day()) !== start.getDay()
        || (this.state.endDate && this.state.endDate.day()) !== end.getDay())) {
      this.setState({
        startDate: moment(start),
        endDate: moment(end)
      }, () => this.props.getServiceResultLists(start, end, this.state.patientId));
    }
  }

  private prepareActorsData = () => {
    const actorsResults = [] as Array<IActorIdWithEvents>;
    // We always want calendar for a patient, and it needs to be first
    actorsResults.push({
      actorId: this.state.patientId,
      actorDisplayName: 'Patient',
      events: []
    })
    this.props.serviceResultLists.forEach((resultList) => {
      var existingIndex = actorsResults.findIndex(a => a.actorId === resultList.actorId);
      var actorDisplayName = this.getActorDisplayName(resultList.actorId, resultList.patientId);
      var actorWithEvents = existingIndex !== -1
        ? actorsResults[existingIndex]
        : {
          actorId: resultList.actorId,
          actorDisplayName: actorDisplayName,
          events: []
        } as IActorIdWithEvents;
      if (this.isServiceSelected(resultList.serviceName)) {
        this.parseEvents(resultList, actorWithEvents.events);
      }
      existingIndex !== -1 ? actorsResults[existingIndex] = actorWithEvents : actorsResults.push(actorWithEvents);
    });

    return this.appendDefaultDummyActorsIfNeeded(actorsResults);
  }

  private getActorDisplayName(actorId: number | null, patientId: number | null): string {
    let displayName = '';
    if (actorId === patientId) {
      return 'Patient';
    }

    if (actorId) {
      const actor = _.find(this.props.actorResultList, actor => actor.actorId === actorId);
      const role = actor && actor.actorTypeName ? actor.actorTypeName! : '';
      const name = actor && actor.actorName ? actor.actorName! : '';
      displayName = this.buildDisplayName(role, name);
    } else {
      console.error('Actor id is not specified');
    }
    return displayName;
  }

  private getDefaultDummyActorResults(): Array<IActorIdWithEvents> {
    return this.props.actorResultList.map(result => {
      return {
        actorId: result.actorId,
        actorDisplayName: this.buildDisplayName(result.actorTypeName, result.actorName),
        events: []
      } as IActorIdWithEvents
    });
  }

  private buildDisplayName(role: string | null, name: string | null): string {
    return _.filter([role, name]).join(' - ');
  }

  private appendDefaultDummyActorsIfNeeded(actorsResults: IActorIdWithEvents[]): Array<IActorIdWithEvents> {  
    return _.unionBy(actorsResults, this.getDefaultDummyActorResults(), 'actorId');
  }

  private parseEvents(resultList: ServiceResultListUI, actorEvents: Array<ICalendarMessageEvent>) {
    resultList.results.forEach((result) => {
      if (result.executionDate !== null) {
        // If there already exists event for an actor with same type and message id we need to group those as it was one message
        var index = actorEvents.findIndex(r => r.executionDate.valueOf() === (result.executionDate && result.executionDate.valueOf())
          && r.messageId === result.messageId);
        // append to existing one
        if (index !== -1) {
          actorEvents[index] = {
            ...actorEvents[index],
            serviceName: `${actorEvents[index].serviceName},\n${resultList.serviceName}`
          };
          // or push new one
        } else {
          actorEvents.push({
            serviceName: resultList.serviceName,
            status: result.serviceStatus,
            executionDate: result.executionDate,
            messageId: result.messageId
          });
        }
      }
    });
  }

  private parseDataToCalendarEvents = (data: Array<ICalendarMessageEvent>) => {
    return data.map(entry => ({
      id: `${entry.messageId.toString()}-${entry.executionDate.valueOf().toString()}`,
      title: '\n' + entry.serviceName,
      status: entry.status,
      start: entry.executionDate.toISOString()
    }))
  }

  private tabSelected = (key) => this.setState({ activeTabKey: key })

  private isServiceSelected = (val: string) => _.has(this.state.filters, val) && !!this.state.filters[val];

  fiterCheckboxChanged = (key: string, val: boolean) => {
    this.setState((prevState: Readonly<ICalendarViewState>) => {
      let oldFilters = prevState.filters;
      oldFilters[key] = val;
      return {
        filters: oldFilters
      }
    })
  }

  renderTemplateFilter = (template: TemplateUI) =>
    <Row key={template.name as string} className="u-pl-1_5em u-mr-0">
      <span>
        <Checkbox
          type="checkbox"
          inline
          className="u-mt-1_4em_neg"
          checked={this.isServiceSelected(template.name as string)}
          onChange={(evt) => this.fiterCheckboxChanged(template.name as string, (evt.target as HTMLInputElement).checked)} />
        {` ${template.name}`}
      </span>
    </Row>

  render() {
    const actorsResults: Array<IActorIdWithEvents> = this.prepareActorsData();

    return (
      <>
        <div className="row">
          <div className="col-md-12 col-xs-12">
            <h3>Calendar Overview</h3>
            <Tabs activeKey={this.state.activeTabKey} onSelect={this.tabSelected} >
              {actorsResults.map((actorWithResults, index) => {
                const tabName = actorWithResults.actorDisplayName;
                return (
                  <Tab title={tabName} key={tabName} eventKey={tabName}>
                    <Row className="u-pl-1em">
                      <Col sm={9}>
                        <a href={`${window.location.href}/patient-template`}>
                          <Button className="btn btn-md pull-right btn-manage-messages">
                            Manage messages
                        </Button>
                        </a>
                        <Calendar
                          dateRangeChangedCallback={(startDate, endDate) => this.dateRangeChanged(startDate, endDate, tabName)}
                          events={this.parseDataToCalendarEvents(actorWithResults.events)}
                          key={tabName}
                        />
                      </Col>
                      <Col sm={3} className="u-p-0 u-mt-4_5em u-mr-0 calendar-filters">
                        <span>
                          <FontAwesomeIcon icon={['fas', 'filter']} />{' '}
                          Display:
                        </span>
                        {_.uniqBy(this.props.templates, 'name').map((template) => this.renderTemplateFilter(template))}
                      </Col>
                    </Row>
                  </Tab>)
              })}
            </Tabs>
          </div>
        </div>
      </>
    );
  }
}

const mapStateToProps = ({ calendar, patientTemplate, actor }: IRootState) => ({
  serviceResultLists: calendar.serviceResultLists,
  templates: patientTemplate.templates,
  loading: patientTemplate.templatesLoading || actor.actorResultListLoading,
  actorResultList: actor.actorResultList
});

const mapDispatchToProps = ({
  getServiceResultLists,
  getTemplates,
  getActorList
});

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(CalendarView);

