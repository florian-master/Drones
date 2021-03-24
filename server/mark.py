class Mark :
    def __init__(self, longitude,latitude,color):
        self.longitude = longitude
        self.latitude = latitude
        self.color = color
    def __eq__(self, value):
        return(self.latitude==value.latitude and self.longitude == value.longitude)