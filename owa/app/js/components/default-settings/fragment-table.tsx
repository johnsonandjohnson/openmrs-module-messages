import React, { ReactFragment } from 'react';
import './fragment-table.scss';
import { IColumn } from '../../shared/model/column.model';
import { IFragmentRow } from '../../shared/model/fragment-table-row.model';

interface IProps {
  columns: ReadonlyArray<IColumn>;
  fragments: ReadonlyArray<IFragmentRow>;
}

export class FragmentTable extends React.Component<IProps> {

  renderColumnWithRows = (column: IColumn) => (
    <>
      <div className="fragment-column">
        <div className="fragment-row even">
          <strong><h4>{column.label}</h4></strong>
        </div>
        {
          this.props.fragments
            .filter((fragment: IFragmentRow) => fragment.key === column.key)
            .map((fragment: IFragmentRow) => fragment.fragments
              .map((fragment: ReactFragment, index: number) => (
                <div key={index} className={`fragment-row ${index % 2 === 0 ? 'odd' : 'even'}`}>
                  {fragment}
                </div>
              )))
        }
      </div>
    </>
  );

  render = () => (
    <div className="fragment-table">
      {this.props.columns.map(this.renderColumnWithRows)}
    </div>
  );
};