    var network;
    var selectedNode = '';

    get("http://localhost:4567/view/name", function (name) {
        document.getElementById('projectName').innerHTML = name
    });
    get("http://localhost:4567/view", showGraph);
    get("http://localhost:4567/view/classpath", showPath);
    get("http://localhost:4567/view/unrefReport", showReport);

    function showPath(value) {
        var response = JSON.parse(value);
        const tplInfo = _.template(document.getElementById('classpathTmpl').innerHTML);
        const reportDiv = document.getElementById('classpath');
        reportDiv.innerHTML=tplInfo({entries: response})
    }

    function showReport(value) {
        var report = JSON.parse(value);
        const tplInfo = _.template(document.getElementById('refReportTmpl').innerHTML);
        const reportDiv = document.getElementById('refReport');
        reportDiv.innerHTML = tplInfo(report);
    }

    function get(url, callBack) {
        var req = new XMLHttpRequest();
        req.open('GET', url, true);
        req.send();
        req.onreadystatechange = function (e) {
            if (req.readyState == 4) {
                callBack(req.responseText);
            }
        }
    }
	var zoomEnabled=true;
	
	function stopMovingNodes(params) {
        network.setOptions({
            physics: false
        });
        console.log("disabled physics");
        zoomEnabled=true;
    }
	
    function showGraph(responseText) {
        var response = JSON.parse(responseText);
        var nodes = new vis.DataSet(response.nodes);
        var edges = new vis.DataSet(response.edges);

        var container = document.getElementById('mynetwork');
        var data = {
            nodes: nodes,
            edges: edges
        };
        var gray = '#555555';
        var options = { 
        		physics: false,
        		interaction: {hover: true},
        		 groups: {
        			 'jar': {
     	            	color: {background:'#e7a5ab',border:gray, hover:{background:'#ebb5ba',border:'black'}, highlight:{background:'#ebb5ba',border:'black'}}
     	            },
     	           'dir': {
   	            	color: {background:'#afd6e7',border:gray, hover:{background:'#dbecf4',border:'black'}, highlight:{background:'#dbecf4',border:'black'}}
                   },
        	            'package': {
        	            	color: {background:'#aec489',border:gray, hover:{background:'#d2debe',border:'black'}, highlight:{background:'#d2debe',border:'black'}}
                        },
        	            'class': {
        	            	color: {background:'#ffed9e',border:gray, hover:{background:'#fff5ca',border:'black'}, highlight:{background:'#fff5ca',border:'black'}}
        	            }
        	            
        	        }
        };
        network = new vis.Network(container, data, options);
        selectedNode = '';
        document.getElementById('description').innerHTML = "Click on node or arc to select it.";
        zoomEnabled=false;
        distribute();
        network.on("stabilized", stopMovingNodes);

        network.on("initRedraw" , function (params) {
            if (!zoomEnabled) {network.fit();}
        });
        network.on("click", function (params) {
            selectedNode = '';
            params.event = "[original event]";
            if (params.nodes.length == 1) {
                getNodeInfo(params.nodes[0]);
            } else if (params.edges.length == 1) {
                getArcInfo(params.edges[0]);
            } else document.getElementById('description').innerHTML = "nothing selected";
        });
    }

    function distribute() {
        network.setOptions({
            physics: true
        });
    }

    function getNodeInfo(id) {
        get("http://localhost:4567/view/node/" + id, function (e) {
            var nodeInfo = JSON.parse(e);
            nodeInfo.id=id;
            
            selectedNode = id;
            const tplInfo = _.template(document.getElementById('nodeInfo').innerHTML);
            const description = document.getElementById('description');
            description.innerHTML = tplInfo(nodeInfo);
            Array.from(description.querySelectorAll('button')).forEach(button=>{
                button.addEventListener('click', function(evt){
                    console.log(evt.target);
                    const mode = evt.target.getAttribute('change-list-mode');
                    changeListMode(mode)
                })
            })
        });
    }

    function changeListMode(mode) {
        if (selectedNode != '') {
            get("http://localhost:4567/view/node/" + selectedNode + "/listmode/" + mode, showGraph);
        }
    }

    function setFilter(value) {
        get("http://localhost:4567/view/filters/" + value, showGraph);
    }

    function impliedOnly(value)
    {
        get("http://localhost:4567/view/filters/impliedBy/"+selectedNode+"/" + value, showGraph);
    }

    function getArcInfo(id) {
        get("http://localhost:4567/view/arc/" + id, function (e) {
            var arcInfo = JSON.parse(e);
            var description = '<h3>' + arcInfo.from.name + ' depends on ' + arcInfo.to.name +
                '</h3> Reason: <table>';
            arcInfo.reason.forEach(function (pair) {
                description = description + '<tr><td>' + pair.first + '</td><td> depends on </td><td>' + pair.second + '</td></tr>'
            });

            document.getElementById('description').innerHTML = description + '</table>';
        });
    }
