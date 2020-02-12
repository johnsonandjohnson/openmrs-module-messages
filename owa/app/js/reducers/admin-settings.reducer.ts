import { REQUEST, SUCCESS, FAILURE } from './action-type.util';
import { handleRequest, handleRequestFailure, initRequestHandling, continueRequestHandling } from '@bit/soldevelo-omrs.cfl-components.request-toast-handler';
import _ from 'lodash';

import 'react-toastify/dist/ReactToastify.css';
import { TemplateUI } from '../shared/model/template-ui';
import axiosInstance from '../config/axios';
import { toModel, mergeWithObjectUIs } from '../shared/model/object-ui';
import * as Msg from '../shared/utils/messages';
import { IDefaultBestContactTime } from '../shared/model/default-best-contact-time.model';
import { IActorType } from '../shared/model/actor-type.model';
import { mapFromRequest, mapToRequest } from '../shared/model/contact-time.model';

export const ACTION_TYPES = {
  GET_TEMPLATES: 'adminSettingsReducer/GET_TEMPLATES',
  GET_ACTOR_TYPES: 'adminSettingsReducer/GET_ACTOR_TYPES',
  GET_DEFAULT_CONTACT_TIMES: 'adminSettingsReducer/GET_DEFAULT_CONTACT_TIMES',
  UPDATE_TEMPLATES: 'adminSettingsReducer/UPDATE_TEMPLATE',
  UPDATE_DEFAULT_CONTACT_TIMES: 'adminSettingsReducer/UPDATE_DEFAULT_CONTACT_TIMES',
  PUT_TEMPLATES: 'adminSettingsReducer/PUT_TEMPLATE',
  PUT_DEFAULT_CONTACT_TIMES: 'adminSettingsReducer/PUT_DEFAULT_CONTACT_TIMES',
  RESET: 'adminSettingsReducer/RESET'
};

const initialState = {
  defaultTemplates: [] as Array<TemplateUI>,
  defaultBestContactTimes: [] as Array<IDefaultBestContactTime>,
  actorTypes: [] as Array<IActorType>,
  loading: false
};

export type AdminSettingsState = Readonly<typeof initialState>;

export default (state = initialState, action): AdminSettingsState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.GET_TEMPLATES):
    case REQUEST(ACTION_TYPES.GET_ACTOR_TYPES):
    case REQUEST(ACTION_TYPES.PUT_TEMPLATES):
    case REQUEST(ACTION_TYPES.GET_DEFAULT_CONTACT_TIMES):
    case REQUEST(ACTION_TYPES.PUT_DEFAULT_CONTACT_TIMES):
      return {
        ...state,
        loading: true
      };
    case FAILURE(ACTION_TYPES.GET_TEMPLATES):
    case FAILURE(ACTION_TYPES.GET_ACTOR_TYPES):
    case FAILURE(ACTION_TYPES.PUT_TEMPLATES):
    case FAILURE(ACTION_TYPES.GET_DEFAULT_CONTACT_TIMES):
    case FAILURE(ACTION_TYPES.PUT_DEFAULT_CONTACT_TIMES):
      return {
        ...state,
        loading: false
      };
    case SUCCESS(ACTION_TYPES.GET_TEMPLATES):
      return {
        ...state,
        loading: false,
        defaultTemplates: _.map(action.payload.data.content, template =>
          TemplateUI.fromModelWithActorTypesExcludingStartOfMessagesField(template, state.actorTypes))
      };
    case SUCCESS(ACTION_TYPES.GET_ACTOR_TYPES):
      return {
        ...state,
        loading: false,
        actorTypes: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.GET_DEFAULT_CONTACT_TIMES):
      return {
        ...state,
        loading: false,
        defaultBestContactTimes: _.map(action.payload.data, mapFromRequest)
      };
    case SUCCESS(ACTION_TYPES.PUT_TEMPLATES):
    case SUCCESS(ACTION_TYPES.PUT_DEFAULT_CONTACT_TIMES):
      return {
        ...state,
        loading: false
      }
    case ACTION_TYPES.UPDATE_TEMPLATES:
      return {
        ...state,
        defaultTemplates: <Array<TemplateUI>>mergeWithObjectUIs(state.defaultTemplates, action.payload as TemplateUI)
      };
    case ACTION_TYPES.UPDATE_DEFAULT_CONTACT_TIMES:
      return {
        ...state,
        defaultBestContactTimes: action.payload
      };
    case ACTION_TYPES.RESET:
      return {
        ..._.cloneDeep(initialState)
      };
    default:
      return state;
  }
};

const baseUrl = "ws/messages";
const templatesUrl = `${baseUrl}/templates`;
const actorUrl = `${baseUrl}/actor`;
const defaultContactTimesUrl = `${actorUrl}/contact-times/default`;
const actorTypesUrl = `${actorUrl}/types`;

export const getConfig = () => async (dispatch) => {
  // Actor types must be first because the result is required in templates mapping
  await dispatch(getActorTypes());
  await dispatch(getTemplates());
  await dispatch(getBestContactTimes());
}

export const getTemplates = () => ({
  type: ACTION_TYPES.GET_TEMPLATES,
  payload: axiosInstance.get(templatesUrl)
});

export const getBestContactTimes = () => async (dispatch) =>
  dispatch({
    type: ACTION_TYPES.GET_DEFAULT_CONTACT_TIMES,
    payload: axiosInstance.get(defaultContactTimesUrl)
  });

export const getActorTypes = () => ({
  type: ACTION_TYPES.GET_ACTOR_TYPES,
  payload: axiosInstance.get(actorTypesUrl)
});

export const updateTemplate = (template: TemplateUI) => async (dispatch) =>
  dispatch({
    type: ACTION_TYPES.UPDATE_TEMPLATES,
    payload: await template
  });

export const updateBestContactTime = (defaultBestContactTimes: Array<IDefaultBestContactTime>) => async (dispatch) =>
  dispatch({
    type: ACTION_TYPES.UPDATE_DEFAULT_CONTACT_TIMES,
    payload: await defaultBestContactTimes
  });

export const saveConfig = (templates: Array<TemplateUI>, contactTimes: Array<IDefaultBestContactTime>) => async (dispatch) => {
  const toastId = initRequestHandling();
  await dispatch(putDefaultContactTimes(contactTimes)).then(
    response => dispatch(putTemplates(templates)).then(
      response => continueRequestHandling(toastId, dispatch, getConfig(), Msg.GENERIC_SUCCESS, Msg.GENERIC_FAILURE),
      error => handleRequestFailure(error, toastId, Msg.GENERIC_FAILURE)),
    error => handleRequestFailure(error, toastId, Msg.GENERIC_FAILURE))
}

export const putDefaultContactTimes = (contactTimes: Array<IDefaultBestContactTime>) => ({
  type: ACTION_TYPES.PUT_DEFAULT_CONTACT_TIMES,
  payload: axiosInstance.put(defaultContactTimesUrl, { records: _.map(contactTimes, mapToRequest) })
});

export const putTemplates = (templates: Array<TemplateUI>) => ({
  type: ACTION_TYPES.PUT_TEMPLATES,
  payload: axiosInstance.put(templatesUrl, { templates: _.map(templates, toModel) })
});

export const reset = () => ({
  type: ACTION_TYPES.RESET
});
