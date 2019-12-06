import React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router-dom';
import { IRootState } from '../../reducers';
import './patient-template.scss';
import BestContactTime from './best-contact-time';
import ScheduledMessages from './scheduled-messages';

interface IManageMessagesProps extends DispatchProps, StateProps, RouteComponentProps<{ patientId: string }> {
};

interface IManageMessagesState {
};

class ManageMessages extends React.PureComponent<IManageMessagesProps, IManageMessagesState> {
  render() {
    const patientId = this.props.match.params.patientId;
    return (
      <div className="body-wrapper">
        <h2>Manage messages</h2>
        <p>For patient {patientId}</p>
        <div className="panel-body">
          <BestContactTime />
          <ScheduledMessages patientId={patientId} />
        </div>
      </div>
    );
  }
}

const mapStateToProps = ({ patientTemplate }: IRootState) => ({
});

const mapDispatchToProps = ({
});

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ManageMessages);
