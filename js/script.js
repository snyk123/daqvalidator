var menulinks = "";
var ser = "";
var hasLoadedFile = false;
var uid = "";

var dimensionHashMap = new Hash();
var metricHashMap = new Hash();

$(document).ready(function() {
	$.getJSON( "params.json", function( data ) {
		// load footer items
		$("#lastUpdateDate").text(data['footer']['lastUpdateDate']);
		
		footerLinks = "";
		links = data['footer']['links'];
		$.each(links, function(item) {
			footerLinks = footerLinks.concat("<a href='"+links[item]['link']+".html'>"+links[item]['name']+"</a> | ");
		});
		$("#links").html(footerLinks);
	});	
	
	//hide other visualise and edit boxes
	$("#_visualisation").hide()
});

function loadFile(path){
	var stringData = $.ajax({
	      url: path,
	      async: false
	}).responseText;
	
	return stringData;
}

$(function() {
    $("#popup").modal();
});



function loadPage(id){
	
	if (id === "validate"){
		$("#_validate").show()
		$("#_visualisation").hide()
	}
	
	if (id === "visualise"){
		if (uid === ""){
			$("#main-content").append("<div class='modal fade hide' id='popup'><div class='modal-dialog'><div class='modal-content'><div class='modal-header'><button type='button' class='close' data-dismiss='modal' aria-hidden='true'>×</button><h4 class='modal-title'>No Model Loaded</h4></div> <div class='modal-body'> <p>A Schema should be loaded before visualising the schema</p> </div> </div> </div> </div><div class='modal-backdrop fade in'></div>");
		} else {
			$("#_validate").hide()
			$("#_visualisation").show()
		}
	}
	
}


function callAPI(type,input){
	
	var urlLocal = "http://localhost:8080/daqvalidator/validate";
	var urlCloud = "http://daqvalidator-jerdeb.rhcloud.com/daqvalidator/validate";

	if (input == ""){
		$("#main-content").append("<div class='modal fade in' style='display:block' aria-hidden='false' id='popup'><div class='modal-dialog'><div class='modal-content'><div class='modal-header'><button type='button' class='close' data-dismiss='modal' aria-hidden='true'>×</button><h4 class='modal-title'>No Schema Error</h4></div> <div class='modal-body'> <p>There is no schema to validate.</p> </div> </div> </div> </div><div class='modal-backdrop fade in'></div>");
	}
	else{
		$.ajax(urlCloud, {
			type:"POST",
			dataType:"json",
			data:{
				"type":type,
				"input":$("#"+input).val()
			}, 
			success:function(data, textStatus, jqXHR) {
				visualise(data);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				$("#main-content").append("<div class='modal fade in' style='display:block' aria-hidden='false' id='popup'><div class='modal-dialog'><div class='modal-content'><div class='modal-header'><button type='button' class='close' data-dismiss='modal' aria-hidden='true'>×</button><h4 class='modal-title'>Service Not Available</h4></div> <div class='modal-body'> <p>Cannot connect to the Service. Please try again later.</p> </div> </div> </div> </div><div class='modal-backdrop fade in'></div>");
			}
		});
	}
	
}


function visualise(data){
	uid = data.uid;
	//check if we've got an error message
	if (uid === ""){
		$("#main-content").append("<div class='modal fade in' style='display:block' aria-hidden='false' id='popup'><div class='modal-dialog'><div class='modal-content'><div class='modal-header'><button type='button' class='close' data-dismiss='modal' aria-hidden='true'>×</button><h4 class='modal-title'>REST API Error</h4></div> <div class='modal-body'> <p>"+data.errormessage+"</p> </div> </div> </div> </div><div class='modal-backdrop fade in'></div>");
	} else {
		// visualise
		$("#_validate").hide();
		$("#_visualisation").show();
		
		//categories	
	    $.each(data.category, function() {
			$("#category").append("<a href='#' class='list-group-item' onClick='getDim(\""+this.name+"\")'><span class='badge'>"+this.dimensions.length+"</span>"+this.name+"</a>");
	    	dimensionHashMap.setItem(this.name, this.dimensions);
		});
		
		//errors
		var counter = 1;
		var totalErrors = data.errors.total;
		if (totalErrors > 0){
		    $.each(data.errors.messages, function() {
				$("#tbody").append("<tr class='danger'><td>"+counter+"</td><td>"+this+"</td></tr>");
				counter = counter + 1;
			});
		} else {
			$("#errorbox").hide();
		}
		
		//warnings
		var totalWarnings = data.warnings.total;
		if (totalWarnings > 0){
		    $.each(data.warnings.messages, function() {
				$("#tbody").append("<tr class='warning'><td>"+counter+"</td><td>"+this+"</td></tr>");
				counter = counter + 1;
			});
		} else {
			$("#warningbox").hide();
		}
	}
}

function getDim(category){
	var dimensions = dimensionHashMap.getItem(category);
	$("#dimension").html("");
    $.each(dimensions, function() {
		$("#dimension").append("<a href='#' class='list-group-item' onClick='getMet(\""+this.name+"\")'><span class='badge'>"+this.metrics.length+"</span>"+this.name+"</a>");
		metricHashMap.setItem(this.name, this.metrics)
	});
}

function getMet(dimension){
	var metrics = metricHashMap.getItem(dimension);
	$("#metric").html("");
    $.each(metrics, function() {
		$("#metric").append("<span class='list-group-item'>"+this.name+"</span>");
	});
}
