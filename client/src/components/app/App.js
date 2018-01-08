import React, { Component } from 'react';
import PropTypes from 'prop-types';

import {
  NavbarToggler,
  NavbarBrand,
  Container,
  Collapse,
  NavItem,
  NavLink,
  Navbar,
  Nav,
  Row,
  Col,
} from 'reactstrap';

import {
  Switch,
  Route,
  Link,
} from 'react-router-dom';

import 'bootstrap/dist/css/bootstrap.css';
import './app.css';

import Home from '../home/Home';

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      navbarOpen: false,
      paths: {
        home: {
          value: '/',
          active: false,
        },
        game: {
          value: '/game',
          active: false,
        },
      },
    };
  }

  componentWillMount() {
    const { pathname } = this.props.location;
    const { paths } = this.state;

    Object.keys(paths).every((key) => {
      const path = paths[key];

      if (pathname === path.value) {
        path.active = true;
        return false;
      }
      return true;
    });

    this.setState({
      paths,
    });
  }

  toggleNavbar() {
    this.setState({
      navbarOpen: !this.state.navbarOpen,
    });
  }

  render() {
    const { home, game } = this.state.paths;

    return (
      <div>
        <Navbar className="bg-light" expand="sm" light>
          <NavbarBrand tag={Link} to="/">
            Knug
          </NavbarBrand>
          <NavbarToggler onClick={() => this.toggleNavbar()} />
          <Collapse isOpen={this.state.navbarOpen} navbar>
            <Nav navbar>
              <NavItem active={home.active}>
                <NavLink tag={Link} to={home.value}>Home</NavLink>
              </NavItem>
              <NavItem active={game.active}>
                <NavLink tag={Link} to={game.value}>Game</NavLink>
              </NavItem>
            </Nav>
          </Collapse>
        </Navbar>
        <Container fluid>
          <Row>
            <Col xs="12">
              <Switch>
                <Route path="/" component={Home} />
              </Switch>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}

App.propTypes = {
  location: PropTypes.shape({
    pathname: PropTypes.string.isRequired,
  }).isRequired,
};

export default App;
