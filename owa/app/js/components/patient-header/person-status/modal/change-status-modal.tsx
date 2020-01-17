/* * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import React from 'react';
import _ from 'lodash';
import {
  Modal,
  Button,
  Form,
  ControlLabel,
  FormGroup,
  FormControl,
  Col
} from 'react-bootstrap';
import ErrorDesc from '@bit/soldevelo-omrs.cfl-components.error-description';
import FormLabel from '@bit/soldevelo-omrs.cfl-components.form-label';
import * as Msg from '../../../../shared/utils/messages';
import { PersonStatusUI } from '../../../../shared/model/person-status.model-ui';
import { IPersonStatusEntry } from '../../../../shared/model/person-status-entry.model';

interface IChangeStatusProps {
  submitDisabled: boolean,
  show: boolean,
  status: PersonStatusUI
  confirm: (value: string, reason?: string) => void
  cancel: () => void
}

interface IChangeStatusState {
  statusValue: string,
  statusReason?: string,
  errors: Object,
  possibleStatuses: Array<IPersonStatusEntry>
}

const FORM_CLASS = 'form-control';
const ERROR_FORM_CLASS = FORM_CLASS + ' error-field';

class ChangeStatusModal extends React.PureComponent<IChangeStatusProps, IChangeStatusState> {

  constructor(props: IChangeStatusProps) {
    super(props);
    this.state = {
      statusValue: props.status.value,
      statusReason: props.status.reason,
      errors: {},
      possibleStatuses: [ Msg.PERSON_STATUSES.ACTIVATED, Msg.PERSON_STATUSES.DEACTIVATED ]
    };
  }

  componentDidUpdate(prevProps: IChangeStatusProps) {
    const { status } = this.props;
    if (status != prevProps.status) {
      this.setState({ statusValue: status.value, statusReason: status.reason });
    }
  };
  
  handleChange = (newValue, prop: string) => {
    const cloned = _.cloneDeep(this.state);
    cloned[prop] = newValue;
    this.setState(cloned);
  }

  renderTextField = (label: string, fieldName: string, required?: boolean) =>
    <FormGroup controlId={fieldName}>
      <FormLabel label={label} mandatory={required} />
      <FormControl
        type="text"
        name={fieldName}
        value={!!this.state[fieldName] ? this.state[fieldName] : ''}
        onChange={e => this.handleChange((e.target as HTMLInputElement).value, fieldName)}
        className={this.state.errors[fieldName] ? ERROR_FORM_CLASS : FORM_CLASS}>
      </FormControl>
      {<ErrorDesc field={this.state.errors[fieldName]} />}
    </FormGroup>

  renderDropdown = (label: string, fieldName: string, options: Array<React.ReactFragment>, required?: boolean) =>
    <FormGroup controlId={fieldName}>
      <FormLabel label={label} mandatory={required} />
      <FormControl componentClass="select" name={fieldName}
        value={this.state[fieldName]}
        onChange={e => this.handleChange((e.target as HTMLInputElement).value, fieldName)}
        className={this.state.errors[fieldName] ? ERROR_FORM_CLASS : FORM_CLASS}>
        {options}
      </FormControl>
      {<ErrorDesc field={this.state.errors[fieldName]} />}
    </FormGroup>

  renderStatusField = () =>
    this.renderDropdown(Msg.PERSON_STATUS_MODAL_FIELD_LABEL, 'statusValue',
      this.state.possibleStatuses.map(status =>
        <option value={status.value} key={status.value}>{status.label}</option>
      ), true);

  renderReasonField = () =>
    this.renderTextField(
      Msg.PERSON_STATUS_MODAL_REASON_FIELD_LABEL,
      'statusReason',
      this.state.statusValue === Msg.DEACTIVATED_KEY);

  buildContext = () => {
    return (
      <Form className="cfl-dialog-content">
        <div className="cfl-dialog-instructions">
          {Msg.PERSON_STATUS_MODAL_INSTRUCTION}
        </div>
        {this.renderStatusField()}
        {this.state.statusValue === Msg.DEACTIVATED_KEY && this.renderReasonField()}
      </Form>
    );
  };

  buildTitle = () => {
    return (
      <>
        <i className="icon-user"></i>
        {Msg.PERSON_STATUS_MODAL_LABEL}
      </>
    );
  };

  buildConfirmButton = () => {
    const { submitDisabled } = this.props;
    return (
      <>
        {Msg.CONFIRM}
        {submitDisabled && <i className="icon-spinner icon-spin icon-2x" />}
      </>
    );
  };

  validateStatusValue = (errors) => {
    delete errors['statusValue'];
    if (!this.state.statusValue) {
      errors['statusValue'] = Msg.FIELD_REQUIRED;
    }
  };

  validateStatusReason = (errors) => {
    delete errors['statusReason'];
    if (this.state.statusValue === Msg.DEACTIVATED_KEY && !this.state.statusReason) {
      errors['statusReason'] = Msg.FIELD_REQUIRED;
    }
  }

  isFormValid = (errors): boolean =>
    _.isEmpty(Object.keys(errors));

  validateForm = (): boolean => {
    const errors = [] as Array<string>;
    this.validateStatusValue(errors);
    this.validateStatusReason(errors);
    this.handleChange(errors, 'errors');
    return this.isFormValid(errors);
  };

  handleConfirm = () => {
    this.validateForm() && this.props.confirm(this.state.statusValue, this.state.statusReason);
  };

  buildModal = () => {
    const { submitDisabled, show } = this.props;
    return (
      <Modal show={show} onHide={this.props.cancel}>
        <Modal.Header bsClass="cfl-dialog-header">
          <Modal.Title>{this.buildTitle()}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {this.buildContext()}
          <Button bsClass="button confirm right"
            onClick={this.handleConfirm}
            disabled={submitDisabled ? true : false}>
            {this.buildConfirmButton()}
          </Button>
          <Button bsClass="button cancel" onClick={this.props.cancel}>
            {Msg.CANCEL}
          </Button>
        </Modal.Body>
      </Modal>
    );
  };

  render() {
    const { show } = this.props;
    return (
      <>
        {show && this.buildModal()}
      </>
    );
  };
}


export default ChangeStatusModal;
