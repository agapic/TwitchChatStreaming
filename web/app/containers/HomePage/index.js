/*
 * HomePage
 *
 */
 /* eslint no-unused-vars:0 */
/* eslint react/no-unused-prop-types:0 */
/* eslint no-undef:0 */
import React from 'react';
import Helmet from 'react-helmet';
import { FormattedMessage } from 'react-intl';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';

import AtPrefix from './AtPrefix';
import CenteredSection from './CenteredSection';
import Form from './Form';
import H2 from 'components/H2';
import Input from './Input';
import List from 'components/List';
import ListItem from 'components/ListItem';
import LoadingIndicator from 'components/LoadingIndicator';
import RepoListItem from 'containers/RepoListItem';
import WordCountListItem from 'containers/WordCountListItem';
import ChannelCountListItem from 'containers/ChannelCountListItem';
import Section from './Section';
import messages from './messages';
import { loadRepos } from '../App/actions';
import { changeUsername } from './actions';
import { selectUsername } from './selectors';
import { selectRepos, selectLoading, selectError } from 'containers/App/selectors';
import lodash from 'lodash';
import io from 'socket.io-client';
const socket = io();
// const socket = io();
// const io = '/socket.io/socket.io.js';
// const socket = io('http://localhost:3000');

export class HomePage extends React.PureComponent { // eslint-disable-line react/prefer-stateless-function
  constructor(props) {
    super(props);
    this.state = { channels: '', words: '' };
  }

  componentDidMount() {
    const that = this;
    if (!this.state.wordCounts) {
      socket.on('wordCounts', function (data) {
        that.setState({ words: data });
      });
    }
    if (!this.state.channelCounts) {
      socket.on('channelCounts', function (data) {
        that.setState({ channels: data });
      });
    }
  }

  render() {
    let channelCounts = null;
    let wordCounts = null;
    let mainContent = null;
    if (this.props.error !== false) {
      const ErrorComponent = () => (
        <ListItem item={'Something went wrong, please try again!'} />
      );
      mainContent = (<List component={ErrorComponent} />);

    // If we're not loading, don't have an error and there are repos, show the repos
    } else {
      /* eslint no-lonely-if:0 */
      if (this.state.channels) {
        channelCounts = (<List items={this.state.channels} component={ChannelCountListItem} />);
      }

      if (this.state.words) {
        wordCounts = (<List items={this.state.words} component={WordCountListItem} />);
      }
    }
    return (
      <div>
        <Helmet
          title="Homepage"
          meta={[
            { name: 'description', content: 'Feature page of React.js Boilerplate application' },
          ]}
        />
        <H2> About </H2>
        <ul>
          <li> Streams <b> real-time </b> chat data from Twitch.tv for the <b> past hour. </b> </li>
          <li> Each data pipeline contains the <b> top 50 </b> results. </li>
          <li> Built with Apache Spark, Redis, React.js, and Node.js </li>
        </ul>
        {mainContent}
        <H2>
          Most Active Channels
        </H2>
        {channelCounts}
        <H2>
          Most Popular Words
        </H2>
        {wordCounts}
      </div>
    );
  }
}

HomePage.propTypes = {
  loading: React.PropTypes.bool,
  error: React.PropTypes.oneOfType([
    React.PropTypes.object,
    React.PropTypes.bool,
  ]),
  repos: React.PropTypes.oneOfType([
    React.PropTypes.array,
    React.PropTypes.bool,
  ]),
  onSubmitForm: React.PropTypes.func,
  username: React.PropTypes.string,
  onChangeUsername: React.PropTypes.func,
};

export function mapDispatchToProps(dispatch) {
  return {
    onChangeUsername: (evt) => dispatch(changeUsername(evt.target.value)),
    onSubmitForm: (evt) => {
      if (evt !== undefined && evt.preventDefault) evt.preventDefault();
      dispatch(loadRepos());
    },
  };
}

const mapStateToProps = createStructuredSelector({
  repos: selectRepos(),
  username: selectUsername(),
  loading: selectLoading(),
  error: selectError(),
});

// Wrap the component to inject dispatch and state into it
export default connect(mapStateToProps, mapDispatchToProps)(HomePage);
