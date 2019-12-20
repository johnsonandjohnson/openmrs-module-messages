import Header from '../patient-header/header'
import React from 'react'
import CalendarView from '../calendar/calendar';
import PatientTemplateEdit from '../patient-template/patient-template-edit';
import { RouteComponentProps } from 'react-router-dom';
import ManageMessages from '../patient-template/manage-messages';

interface IWrappedComponentProps extends RouteComponentProps<{ patientuuid: string }> {
  isNew?: boolean;
}

const withPatientHeader = (WrappedComponent) => {
  return class extends React.Component<IWrappedComponentProps> {
    render() {
      return (
          <div className="body-wrapper">
            <Header patientuuid={this.props.match.params.patientuuid} />
            <div className="content">
              <WrappedComponent  {...this.props}/>
            </div>
          </div>
      )
    }
  }
}

export const CalendarWithHeader = withPatientHeader(CalendarView); 
export const PatientTemplateEditWithHeader = withPatientHeader(PatientTemplateEdit); 
export const ManageMessagesWithHeader = withPatientHeader(ManageMessages); 